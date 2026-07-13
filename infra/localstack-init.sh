#!/bin/bash

set -e

echo "Initializing LocalStack AWS Services..."

# Wait for LocalStack to be ready
sleep 5

# Create S3 bucket
echo "Creating S3 bucket: ticketwave-bucket"
awslocal s3 mb s3://ticketwave-bucket || echo "Bucket already exists"

# Create SQS queue for processing
echo "Creating SQS queue: ticketwave-queue"
awslocal sqs create-queue --queue-name ticketwave-queue || echo "Queue already exists"

# Create SNS topic for notifications
echo "Creating SNS topic: ticketwave-notifications"
awslocal sns create-topic --name ticketwave-notifications || echo "Topic already exists"

# Create DynamoDB table for sessions
echo "Creating DynamoDB table: ticketwave-sessions"
awslocal dynamodb create-table \
  --table-name ticketwave-sessions \
  --attribute-definitions AttributeName=sessionId,AttributeType=S \
  --key-schema AttributeName=sessionId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  || echo "Table already exists"

# Enable versioning on S3 bucket
echo "Enabling S3 versioning on ticketwave-bucket"
awslocal s3api put-bucket-versioning \
  --bucket ticketwave-bucket \
  --versioning-configuration Status=Enabled \
  || echo "Versioning already enabled"

# Set S3 bucket to public (for testing)
echo "Setting S3 bucket policy to public"
awslocal s3api put-bucket-acl --bucket ticketwave-bucket --acl public-read || echo "ACL already set"

echo "LocalStack initialization completed successfully!"
