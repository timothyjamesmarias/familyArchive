#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Family Archive - AWS Infrastructure Setup${NC}"
echo "=========================================="
echo ""

# Configuration
AWS_REGION="us-east-1"
ECR_REPO_NAME="family-archive"
LIGHTSAIL_SERVICE_NAME="family-archive-service"
LIGHTSAIL_CAPACITY="small"  # Options: nano, micro, small, medium, large

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI is not installed${NC}"
    echo "Install it with: brew install awscli"
    exit 1
fi

# Check if AWS credentials are configured
if ! aws sts get-caller-identity &> /dev/null; then
    echo -e "${RED}Error: AWS credentials not configured${NC}"
    echo "Run: aws configure"
    exit 1
fi

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo -e "${GREEN}✓${NC} AWS Account ID: $AWS_ACCOUNT_ID"
echo ""

# Step 1: Create ECR repository
echo -e "${YELLOW}Step 1: Creating ECR repository...${NC}"
if aws ecr describe-repositories --repository-names $ECR_REPO_NAME --region $AWS_REGION &> /dev/null; then
    echo -e "${GREEN}✓${NC} ECR repository already exists"
else
    aws ecr create-repository \
        --repository-name $ECR_REPO_NAME \
        --region $AWS_REGION \
        --query 'repository.repositoryUri' \
        --output text
    echo -e "${GREEN}✓${NC} ECR repository created"
fi
echo ""

# Step 2: Create Lightsail Container Service
echo -e "${YELLOW}Step 2: Creating Lightsail Container Service...${NC}"
echo "This will take ~5 minutes..."

if aws lightsail get-container-services --service-name $LIGHTSAIL_SERVICE_NAME --region $AWS_REGION &> /dev/null; then
    echo -e "${GREEN}✓${NC} Lightsail service already exists"
else
    aws lightsail create-container-service \
        --service-name $LIGHTSAIL_SERVICE_NAME \
        --power $LIGHTSAIL_CAPACITY \
        --scale 1 \
        --region $AWS_REGION

    echo "Waiting for service to be ready..."
    while true; do
        STATE=$(aws lightsail get-container-services \
            --service-name $LIGHTSAIL_SERVICE_NAME \
            --region $AWS_REGION \
            --query 'containerServices[0].state' \
            --output text 2>/dev/null || echo "PENDING")

        if [ "$STATE" = "READY" ] || [ "$STATE" = "RUNNING" ]; then
            break
        fi
        echo -n "."
        sleep 10
    done
    echo ""
    echo -e "${GREEN}✓${NC} Lightsail service created"
fi
echo ""

# Step 3: Display next steps
echo -e "${GREEN}=========================================="
echo "Infrastructure Setup Complete!"
echo -e "==========================================${NC}"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo ""
echo "1. Set up RDS PostgreSQL (manual via AWS Console):"
echo "   - Instance: db.t4g.micro"
echo "   - Database name: familyarchive"
echo "   - Username: familyarchive"
echo "   - See DEPLOYMENT.md for detailed instructions"
echo ""
echo "2. Configure GitHub Secrets (Settings > Secrets and variables > Actions):"
echo "   AWS_ACCESS_KEY_ID=<your-access-key>"
echo "   AWS_SECRET_ACCESS_KEY=<your-secret-key>"
echo "   DATABASE_URL=jdbc:postgresql://<rds-endpoint>:5432/familyarchive"
echo "   DATABASE_USERNAME=familyarchive"
echo "   DATABASE_PASSWORD=<your-db-password>"
echo ""
echo "3. Push code to GitHub master/main branch"
echo "   The GitHub Action will automatically build and deploy!"
echo ""
echo -e "${GREEN}Your ECR repository:${NC}"
echo "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME"
echo ""
echo -e "${GREEN}Your Lightsail service:${NC}"
LIGHTSAIL_URL=$(aws lightsail get-container-services \
    --service-name $LIGHTSAIL_SERVICE_NAME \
    --region $AWS_REGION \
    --query 'containerServices[0].url' \
    --output text 2>/dev/null || echo "Not yet deployed")
echo "$LIGHTSAIL_URL"
