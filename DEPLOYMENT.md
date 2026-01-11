# Family Archive - AWS Deployment Guide

This guide covers deploying Family Archive to AWS using **GitHub Actions** (no local Docker required), Lightsail Container Service, and RDS PostgreSQL.

## Architecture Overview

```
GitHub Push → GitHub Actions (Build Docker)
                     ↓
              Amazon ECR (Image Storage)
                     ↓
         Lightsail Container Service
                     ↓
              RDS PostgreSQL
```

**Estimated Monthly Cost:** $32-55/month
- Lightsail Container (Small): $20/month
- RDS db.t4g.micro: $12-15/month
- ECR: ~$0.10/month (negligible)

---

## Prerequisites

- AWS Account with billing enabled
- AWS CLI installed and configured: `aws configure`
- GitHub repository for this project
- Basic familiarity with AWS Console

**Note:** You do NOT need Docker installed locally! GitHub Actions handles all builds.

---

## Quick Start (30 minutes)

### Step 1: Set Up AWS Infrastructure (10 min)

Run the automated setup script:

```bash
./setup-aws.sh
```

This creates:
- ECR repository for Docker images
- Lightsail Container Service

**Manual step:** Create RDS database (see Part 2 below for detailed instructions)

### Step 2: Configure GitHub Secrets (5 min)

In your GitHub repo: **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Add these secrets:

| Secret Name | Value |
|------------|-------|
| `AWS_ACCESS_KEY_ID` | Your AWS access key |
| `AWS_SECRET_ACCESS_KEY` | Your AWS secret key |
| `DATABASE_URL` | `jdbc:postgresql://<RDS_ENDPOINT>:5432/familyarchive` |
| `DATABASE_USERNAME` | `familyarchive` |
| `DATABASE_PASSWORD` | Your RDS password |

**To create AWS keys:**
1. AWS Console → IAM → Users → Your user
2. Security credentials → Create access key
3. Choose "CLI" use case

### Step 3: Deploy! (15 min)

```bash
git add .
git commit -m "Initial deployment setup"
git push origin master
```

GitHub Actions will automatically:
1. Build Docker image (5-10 min)
2. Push to ECR
3. Deploy to Lightsail
4. Report your app URL

Check progress: **GitHub repo → Actions tab**

---

## Part 1: Automated Infrastructure Setup

### Option A: Use the Setup Script (Recommended)

```bash
chmod +x setup-aws.sh
./setup-aws.sh
```

This automatically creates ECR and Lightsail resources.

### Option B: Manual Setup

#### Create ECR Repository

```bash
aws ecr create-repository \
  --repository-name family-archive \
  --region us-east-1
```

#### Create Lightsail Container Service

```bash
aws lightsail create-container-service \
  --service-name family-archive-service \
  --power small \
  --scale 1 \
  --region us-east-1
```

**Power options:**
- `nano` (512MB) - $7/month - Too small for Spring Boot
- `micro` (1GB) - $10/month - Minimum
- `small` (2GB) - $20/month - **Recommended**
- `medium` (4GB) - $40/month - For heavy usage

Wait 5-10 minutes for service to provision.

---

## Part 2: Set Up RDS PostgreSQL

### Create RDS Instance (AWS Console)

1. AWS Console → **RDS** → **Create database**

2. **Engine options:**
   - Engine: `PostgreSQL`
   - Version: `16.x` (latest)

3. **Templates:**
   - `Free tier` or `Dev/Test`

4. **Settings:**
   - DB identifier: `family-archive-db`
   - Master username: `familyarchive`
   - Master password: *Create strong password* (save it!)

5. **Instance configuration:**
   - Class: `db.t4g.micro` (1 vCPU, 1 GB RAM)

6. **Storage:**
   - Allocated: `20 GB`
   - Autoscaling: Enable (max 100 GB)

7. **Connectivity:**
   - Public access: `Yes`
   - VPC security group: `Create new` → Name it `family-archive-db-sg`

8. **Additional configuration:**
   - Initial database name: `familyarchive`
   - Automated backups: Enable (1 day retention)
   - Enhanced monitoring: Disable (save cost)

9. Click **Create database** (takes 5-10 minutes)

### Configure Security Group

**Important:** Allow Lightsail to access RDS

1. AWS Console → **EC2** → **Security Groups**
2. Find `family-archive-db-sg`
3. **Inbound rules** → **Edit**
4. Add rule:
   - Type: `PostgreSQL`
   - Port: `5432`
   - Source: `Anywhere-IPv4` (0.0.0.0/0)
   - Description: `Lightsail access`

**For production:** Restrict to Lightsail IP ranges. For now, this is fine.

### Get RDS Endpoint

1. RDS → Databases → `family-archive-db`
2. Copy **Endpoint** (e.g., `family-archive-db.abc123.us-east-1.rds.amazonaws.com`)
3. Use this for `DATABASE_URL` GitHub secret:
   ```
   jdbc:postgresql://YOUR-ENDPOINT-HERE:5432/familyarchive
   ```

### Test Connection (Optional)

```bash
psql -h <your-rds-endpoint> \
     -U familyarchive \
     -d familyarchive
# Enter password when prompted
```

---

## Part 3: GitHub Actions CI/CD

The workflow file is already created at `.github/workflows/deploy.yml`.

### How it Works

**Triggered by:** Push to `master` or `main` branch

**Steps:**
1. Checkout code
2. Configure AWS credentials (from GitHub secrets)
3. Build Docker image on GitHub's servers
4. Push image to ECR
5. Deploy to Lightsail with environment variables
6. Wait for deployment to complete
7. Output application URL

### View Deployment

**GitHub repo → Actions tab**

You'll see:
- Build logs
- Deployment status
- Application URL when complete

### Manual Trigger

You can also trigger manually:
1. GitHub → Actions → "Build and Deploy to AWS Lightsail"
2. Click "Run workflow"

---

## Part 4: First Deployment

### Deploy Your App

```bash
# Commit your code
git add .
git commit -m "Set up deployment pipeline"

# Push to trigger deployment
git push origin master
```

### Monitor Progress

1. **GitHub Actions tab** - Shows build progress
2. **AWS Lightsail Console** - Shows deployment status
3. Wait 10-15 minutes for first deployment

### Check Your App

After deployment completes, get your URL:

```bash
aws lightsail get-container-services \
  --service-name family-archive-service \
  --query 'containerServices[0].url' \
  --output text
```

Visit the URL in your browser!

---

## Part 5: Database Migrations

Your app uses Flyway for database migrations.

**First deployment:**
1. Container starts
2. Flyway connects to RDS
3. Detects empty database
4. Runs all migrations from `src/main/resources/db/migration/`
5. Creates schema

**View migrations:**

```bash
psql -h <rds-endpoint> -U familyarchive -d familyarchive
```

```sql
SELECT * FROM flyway_schema_history;
```

**Add new migrations:**
1. Create `src/main/resources/db/migration/V2__description.sql`
2. Commit and push
3. GitHub Actions builds and deploys
4. Flyway runs new migration automatically

---

## Part 6: Environment Configuration

### Production Properties

Already included: `src/main/resources/application-prod.properties`

Key settings:
- Profile: `prod` (set via GitHub Actions)
- Hibernate: `validate` (Flyway handles schema)
- Logging: `INFO` level
- Dev tools: Disabled

### Update Environment Variables

To change database credentials or other env vars:

1. Update GitHub Secrets (Settings → Secrets → Actions)
2. Push any commit to trigger redeployment
3. Or manually trigger workflow in Actions tab

---

## Part 7: Monitoring & Troubleshooting

### View Logs

**Via AWS Console:**
1. Lightsail → Containers → `family-archive-service`
2. **Logs** tab

**Via CLI:**

```bash
aws lightsail get-container-log \
  --service-name family-archive-service \
  --container-name family-archive \
  --start-time $(date -u -v-1H +%s)
```

### Check Deployment Status

```bash
aws lightsail get-container-services \
  --service-name family-archive-service \
  --query 'containerServices[0].state'
```

States: `PENDING` → `DEPLOYING` → `RUNNING`

### Common Issues

**GitHub Action fails:**
- Check AWS credentials in GitHub secrets
- Verify IAM permissions (ECR, Lightsail access needed)

**Container won't start:**
- Check logs in Lightsail console
- Verify DATABASE_URL format
- Check RDS security group

**Database connection failed:**
- Verify RDS endpoint in DATABASE_URL
- Check RDS security group allows 0.0.0.0/0 on port 5432
- Confirm RDS status is "Available"

**Application is slow:**
- Upgrade Lightsail tier: `small` → `medium`
- Upgrade RDS: `db.t4g.micro` → `db.t4g.small`

### Health Checks

The container has built-in health checks. Lightsail monitors:
- Path: `/` (homepage)
- Interval: Every 30 seconds
- Timeout: 5 seconds
- Unhealthy after 3 failures

---

## Part 8: Making Updates

### Deploy Code Changes

```bash
# Make your changes
vim src/main/kotlin/...

# Commit and push
git add .
git commit -m "Fix bug in user authentication"
git push origin master

# GitHub Actions automatically rebuilds and redeploys
```

**Deployment time:** 10-15 minutes per deploy

### Rollback to Previous Version

```bash
# Find previous image
aws ecr list-images \
  --repository-name family-archive \
  --region us-east-1

# Update deployment.template.json with old image SHA
# Deploy manually:
aws lightsail create-container-service-deployment \
  --service-name family-archive-service \
  --cli-input-json file://deployment.template.json
```

---

## Part 9: Custom Domain (Optional)

### Add HTTPS and Custom Domain

1. **Lightsail Console** → Your service → **Custom domains**
2. Click **Create certificate**
3. Enter domain: `familyarchive.com`
4. Add validation CNAME to your DNS provider
5. Wait for validation (5-30 min)
6. Attach certificate to service

### Update DNS

Add CNAME record with your DNS provider:

```
Type: CNAME
Name: www (or @)
Value: <lightsail-service-url>
TTL: 3600
```

---

## Part 10: Cost Optimization

**Current setup: ~$32-55/month**

**Ways to reduce:**
1. Use `micro` tier if CPU/RAM usage stays low (<30%)
2. Set RDS backup retention to 1 day
3. Delete old ECR images (keep last 5)
4. Monitor AWS Cost Explorer monthly

**Delete old images:**

```bash
# Keep only last 5 images
aws ecr describe-images \
  --repository-name family-archive \
  --query 'sort_by(imageDetails,& imagePushedAt)[:-5].[imageDigest]' \
  --output text | while read digest; do
    aws ecr batch-delete-image \
      --repository-name family-archive \
      --image-ids imageDigest=$digest
done
```

---

## Part 11: Local Development (Optional)

You don't need Docker for development, but if you want to test the build:

```bash
# Build image locally
docker build -t family-archive:latest .

# Run with local database
docker compose up -d
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/familyarchive \
  -e SPRING_DATASOURCE_USERNAME=familyarchive \
  -e SPRING_DATASOURCE_PASSWORD=familyarchive \
  family-archive:latest
```

But for regular development, just use:

```bash
./dev.sh
```

---

## Quick Reference Commands

### View App URL
```bash
aws lightsail get-container-services \
  --service-name family-archive-service \
  --query 'containerServices[0].url' \
  --output text
```

### View Recent Logs
```bash
aws lightsail get-container-log \
  --service-name family-archive-service \
  --container-name family-archive
```

### Check Deployment Status
```bash
aws lightsail get-container-services \
  --service-name family-archive-service \
  --query 'containerServices[0].{state:state,power:power,scale:scale}'
```

### Connect to Database
```bash
psql -h <rds-endpoint> -U familyarchive -d familyarchive
```

### Trigger Manual Deployment
Go to GitHub → Actions → "Build and Deploy" → "Run workflow"

---

## Cleanup (Delete Everything)

**Warning:** This deletes all data!

```bash
# Delete Lightsail service
aws lightsail delete-container-service \
  --service-name family-archive-service

# Delete RDS database
aws rds delete-db-instance \
  --db-instance-identifier family-archive-db \
  --skip-final-snapshot

# Delete ECR repository
aws ecr delete-repository \
  --repository-name family-archive \
  --force
```

---

## Next Steps

- ✅ Infrastructure set up
- ✅ CI/CD pipeline configured
- ✅ Database connected
- ⬜ Set up custom domain
- ⬜ Configure monitoring/alerts
- ⬜ Add background job processing
- ⬜ Set up database backup testing

---

## Support & Resources

- **AWS Lightsail Docs:** https://docs.aws.amazon.com/lightsail/
- **GitHub Actions Docs:** https://docs.github.com/en/actions
- **Spring Boot Deployment:** https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html
- **AWS ECR:** https://docs.aws.amazon.com/ecr/

---

## Appendix: Workflow File Explained

The `.github/workflows/deploy.yml` file:

- **Triggers:** Push to master/main, or manual
- **Secrets used:** AWS credentials, database config
- **Steps:**
  1. Checkout code from GitHub
  2. Login to AWS and ECR
  3. Build Docker image (no local Docker needed!)
  4. Push to ECR with commit SHA as tag
  5. Create deployment JSON with secrets
  6. Deploy to Lightsail
  7. Wait and report status

**Image tags:** Each build is tagged with `latest` and the git commit SHA for easy rollbacks.
