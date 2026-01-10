# Family Archive - AWS Deployment Guide

This guide covers deploying Family Archive to AWS using Lightsail Container Service and RDS PostgreSQL.

## Architecture Overview

```
User → Lightsail Container Service (Docker)
              ↓
        RDS PostgreSQL
```

**Estimated Monthly Cost:** $32-55
- Lightsail Container (Small): $20/month
- RDS db.t4g.micro: $12-15/month

---

## Prerequisites

- AWS Account with billing enabled
- AWS CLI installed and configured: `aws configure`
- Docker installed locally
- Familiarity with AWS Console

---

## Part 1: Build and Test Docker Image Locally

### 1.1 Build the Docker Image

```bash
# From project root
docker build -t family-archive:latest .
```

**Build time:** 5-10 minutes (first build downloads dependencies)

### 1.2 Test Locally with Docker Compose Database

Start your local PostgreSQL:
```bash
docker compose up -d
```

Run the container:
```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/familyarchive \
  -e SPRING_DATASOURCE_USERNAME=familyarchive \
  -e SPRING_DATASOURCE_PASSWORD=familyarchive \
  family-archive:latest
```

**Note:** `host.docker.internal` allows the container to connect to your host machine's PostgreSQL.

Test the application:
```bash
curl http://localhost:8080
# Should return your homepage HTML
```

If successful, stop the container: `Ctrl+C`

---

## Part 2: Set Up AWS RDS PostgreSQL

### 2.1 Create RDS Instance

1. Open AWS Console → RDS → **Create database**

2. **Engine options:**
   - Engine type: `PostgreSQL`
   - Version: `16.x` (latest 16.x version)

3. **Templates:**
   - Select: `Free tier` or `Dev/Test`

4. **Settings:**
   - DB instance identifier: `family-archive-db`
   - Master username: `familyarchive`
   - Master password: Create a strong password (save this!)

5. **Instance configuration:**
   - DB instance class: `db.t4g.micro` (1 vCPU, 1 GB RAM)

6. **Storage:**
   - Allocated storage: `20 GB` (minimum)
   - Storage autoscaling: Enable (max 100 GB)

7. **Connectivity:**
   - Public access: `Yes` (we'll secure with security group)
   - VPC security group: Create new `family-archive-db-sg`

8. **Database authentication:**
   - Password authentication

9. **Additional configuration:**
   - Initial database name: `familyarchive`
   - Enable automated backups (1 day retention minimum)
   - Disable Enhanced Monitoring (to save cost)

10. Click **Create database**

**Wait time:** 5-10 minutes for RDS to become available

### 2.2 Configure Security Group

1. Go to **EC2 → Security Groups**
2. Find `family-archive-db-sg`
3. Edit **Inbound rules** → Add rule:
   - Type: `PostgreSQL`
   - Port: `5432`
   - Source: `Custom` → Will add Lightsail IP later
   - Description: `Lightsail container access`

**For initial testing (temporary):**
- Add your IP: `My IP` (to test connection from your machine)

### 2.3 Get RDS Connection Details

1. Go to RDS → Databases → `family-archive-db`
2. Copy the **Endpoint** (looks like: `family-archive-db.xxxxx.us-east-1.rds.amazonaws.com`)
3. Note the **Port** (default: `5432`)

### 2.4 Test RDS Connection (Optional)

```bash
# Install psql if needed: brew install postgresql
psql -h <your-rds-endpoint> \
     -U familyarchive \
     -d familyarchive

# Enter password when prompted
# If successful, you'll see: familyarchive=>
# Type \q to quit
```

---

## Part 3: Deploy to AWS Lightsail Container Service

### 3.1 Create Lightsail Container Service

1. Open AWS Console → **Lightsail** → **Containers** → **Create container service**

2. **Location:**
   - Select region closest to you (or `us-east-1`)

3. **Container service capacity:**
   - Choose your capacity:
     - **Nano** (512 MB, 0.25 vCPU) - $7/month - May be too small for Spring Boot
     - **Micro** (1 GB, 0.5 vCPU) - $10/month - Minimum recommended
     - **Small** (2 GB, 1 vCPU) - $20/month - **Recommended**
     - **Medium** (4 GB, 2 vCPU) - $40/month - If you need extra headroom

4. **Deployment:**
   - Select: `Specify a custom deployment`

5. **Container name:** `family-archive`

6. **Image:** Leave empty for now (we'll push via CLI)

7. **Container service name:** `family-archive-service`

8. Click **Create container service**

**Wait time:** 5 minutes for service to provision

### 3.2 Push Docker Image to Lightsail

```bash
# Install AWS Lightsail plugin (one-time)
aws lightsail push-container-image --help

# Login and push image
aws lightsail push-container-image \
  --service-name family-archive-service \
  --label family-archive \
  --image family-archive:latest
```

**Output will show:**
```
...
Digest: sha256:xxxxx
Image uploaded: :family-archive-service.family-archive.X
```

**Copy the full image name** (`:family-archive-service.family-archive.X`)

### 3.3 Create Container Deployment Configuration

Create a file `deployment.json`:

```json
{
  "containers": {
    "family-archive": {
      "image": ":family-archive-service.family-archive.1",
      "environment": {
        "SPRING_DATASOURCE_URL": "jdbc:postgresql://<YOUR-RDS-ENDPOINT>:5432/familyarchive",
        "SPRING_DATASOURCE_USERNAME": "familyarchive",
        "SPRING_DATASOURCE_PASSWORD": "<YOUR-RDS-PASSWORD>",
        "SPRING_PROFILES_ACTIVE": "prod",
        "SPRING_JPA_HIBERNATE_DDL_AUTO": "validate"
      },
      "ports": {
        "8080": "HTTP"
      }
    }
  },
  "publicEndpoint": {
    "containerName": "family-archive",
    "containerPort": 8080,
    "healthCheck": {
      "path": "/",
      "intervalSeconds": 30,
      "timeoutSeconds": 5,
      "successCodes": "200-499"
    }
  }
}
```

**Replace:**
- `<YOUR-RDS-ENDPOINT>` with your RDS endpoint
- `<YOUR-RDS-PASSWORD>` with your RDS password
- Image name (`.1`) with the number from step 3.2

### 3.4 Deploy the Container

```bash
aws lightsail create-container-service-deployment \
  --service-name family-archive-service \
  --cli-input-json file://deployment.json
```

**Deployment status:**
```bash
# Check deployment status
aws lightsail get-container-services \
  --service-name family-archive-service \
  --query 'containerServices[0].state'

# Wait for: "RUNNING"
```

### 3.5 Update RDS Security Group

Now that Lightsail is running, we need to allow it to access RDS.

**Option 1: Use NAT Gateway IP (More secure but complex)**
- Requires VPC peering between Lightsail and RDS VPC

**Option 2: Allow Lightsail public IPs (Simpler)**

1. Get your Lightsail container's public endpoint:
   ```bash
   aws lightsail get-container-services \
     --service-name family-archive-service \
     --query 'containerServices[0].url' \
     --output text
   ```

2. Get Lightsail's outbound IP ranges for your region from AWS docs
3. Add to RDS security group inbound rules

**Option 3: Temporary - Allow all (NOT RECOMMENDED FOR PRODUCTION)**
- Security Group Inbound: `0.0.0.0/0` on port 5432
- **Only use for initial testing**, then restrict to Lightsail IPs

### 3.6 Get Your Application URL

```bash
aws lightsail get-container-services \
  --service-name family-archive-service \
  --query 'containerServices[0].url' \
  --output text
```

**Output:** `https://family-archive-service.xxxxx.us-east-1.cs.amazonlightsail.com`

Visit this URL in your browser!

---

## Part 4: Database Migrations (Flyway)

Your application includes Flyway migrations that run automatically on startup.

**First deployment:**
1. Application starts
2. Flyway detects empty database
3. Runs all migrations in `src/main/resources/db/migration/`
4. Creates `flyway_schema_history` table

**View migration status:**

```bash
# Connect to RDS
psql -h <your-rds-endpoint> -U familyarchive -d familyarchive

# Check migration history
SELECT * FROM flyway_schema_history;
```

**Adding new migrations:**
1. Create new file: `src/main/resources/db/migration/V2__description.sql`
2. Rebuild Docker image
3. Redeploy to Lightsail
4. Flyway automatically runs new migrations

---

## Part 5: Environment-Specific Configuration

### Production Application Properties

Create `src/main/resources/application-prod.properties`:

```properties
# Server
server.port=8080

# Database (override via environment variables)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Flyway
spring.flyway.enabled=true

# Disable dev tools
spring.devtools.restart.enabled=false

# Logging
logging.level.root=INFO
logging.level.com.timothymarias.familyarchive=INFO

# Disable seeding
app.db.seed-enabled=false
```

Rebuild and redeploy after adding this file.

---

## Part 6: CI/CD Pipeline (GitHub Actions)

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to AWS Lightsail

on:
  push:
    branches: [ master ]

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Build Docker image
      run: docker build -t family-archive:latest .

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: us-east-1

    - name: Push to Lightsail
      run: |
        aws lightsail push-container-image \
          --service-name family-archive-service \
          --label family-archive \
          --image family-archive:latest

    - name: Deploy to Lightsail
      run: |
        # Get latest image version
        IMAGE=$(aws lightsail get-container-images \
          --service-name family-archive-service \
          --query 'containerImages[0].image' \
          --output text)

        # Update deployment.json with new image
        jq --arg img "$IMAGE" \
          '.containers["family-archive"].image = $img' \
          deployment.json > deployment-updated.json

        # Deploy
        aws lightsail create-container-service-deployment \
          --service-name family-archive-service \
          --cli-input-json file://deployment-updated.json
```

**GitHub Secrets to add:**
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

---

## Part 7: Monitoring and Logs

### View Container Logs

**Via CLI:**
```bash
aws lightsail get-container-log \
  --service-name family-archive-service \
  --container-name family-archive \
  --start-time $(date -u -v-1H '+%Y-%m-%dT%H:%M:%S')
```

**Via Console:**
1. Lightsail → Containers → `family-archive-service`
2. Click **Metrics** tab
3. Click **Logs** tab

### Key Metrics to Monitor

- **CPU utilization:** Should stay under 50% normally
- **Memory utilization:** Spring Boot typically uses 70-80% of allocated RAM
- **Request count:** Track traffic patterns
- **Error rate:** Should be near 0%

---

## Part 8: Custom Domain (Optional)

### 8.1 Enable HTTPS and Custom Domain

1. Lightsail Console → Your service → **Custom domains** tab
2. Click **Create certificate**
3. Enter your domain (e.g., `familyarchive.com`)
4. Add validation CNAME records to your DNS
5. Wait for validation (5-30 minutes)
6. Attach certificate to your service
7. Add CNAME record pointing to Lightsail URL

**DNS Record:**
```
Type: CNAME
Name: www (or @)
Value: <your-lightsail-url>
```

---

## Part 9: Troubleshooting

### Container Won't Start

**Check logs:**
```bash
aws lightsail get-container-log \
  --service-name family-archive-service \
  --container-name family-archive
```

**Common issues:**
- Database connection failure → Check RDS security group
- Out of memory → Upgrade to larger Lightsail tier
- Missing environment variables → Check deployment.json

### Database Connection Issues

**Test from local:**
```bash
psql -h <rds-endpoint> -U familyarchive -d familyarchive
```

**If fails:**
- Check RDS security group allows your IP
- Verify endpoint and credentials
- Check RDS is in "Available" state

**From Lightsail:**
- Verify security group allows Lightsail IPs
- Check environment variables in deployment.json

### Application is Slow

**Potential fixes:**
1. Upgrade Lightsail tier (Small → Medium)
2. Upgrade RDS instance (db.t4g.micro → db.t4g.small)
3. Add database connection pooling configuration
4. Enable query logging to find slow queries

### Redeploy After Code Changes

```bash
# 1. Rebuild Docker image
docker build -t family-archive:latest .

# 2. Push to Lightsail
aws lightsail push-container-image \
  --service-name family-archive-service \
  --label family-archive \
  --image family-archive:latest

# 3. Update deployment.json with new image version

# 4. Deploy
aws lightsail create-container-service-deployment \
  --service-name family-archive-service \
  --cli-input-json file://deployment.json
```

---

## Part 10: Cost Optimization Tips

1. **Use db.t4g instances (ARM)** - 20% cheaper than db.t3
2. **Enable RDS storage autoscaling** - Only pay for what you use
3. **Set RDS backup retention to 1 day** - Reduces backup storage costs
4. **Monitor CPU/RAM usage** - Downgrade if consistently under 30%
5. **Use Lightsail instead of ECS/Fargate** - Much simpler, cheaper for small apps
6. **Reserved capacity** - If running 24/7 for a year, can save 30-40%

---

## Quick Reference

### Update Environment Variable

```bash
# Edit deployment.json, then:
aws lightsail create-container-service-deployment \
  --service-name family-archive-service \
  --cli-input-json file://deployment.json
```

### View Current Deployment

```bash
aws lightsail get-container-services \
  --service-name family-archive-service
```

### Delete Everything (Clean Up)

```bash
# Delete Lightsail service
aws lightsail delete-container-service \
  --service-name family-archive-service

# Delete RDS (from Console or CLI)
aws rds delete-db-instance \
  --db-instance-identifier family-archive-db \
  --skip-final-snapshot
```

---

## Next Steps

1. ✅ Deploy to Lightsail
2. ✅ Connect to RDS
3. Set up custom domain
4. Configure GitHub Actions CI/CD
5. Add monitoring/alerting
6. Set up automated database backups testing
7. Implement background job processing

---

## Support

- **AWS Lightsail Docs:** https://docs.aws.amazon.com/lightsail/
- **RDS Docs:** https://docs.aws.amazon.com/rds/
- **Spring Boot Deployment:** https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html
