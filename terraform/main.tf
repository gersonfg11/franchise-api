terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  required_version = ">= 1.0"
}

provider "aws" {
  region = var.aws_region
}

# ── DynamoDB ──────────────────────────────────────────────────────────────────

resource "aws_dynamodb_table" "franchises" {
  name         = var.table_name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Environment = var.environment
    Project     = "franchise-api"
    ManagedBy   = "terraform"
  }
}

# ── ECR ───────────────────────────────────────────────────────────────────────

resource "aws_ecr_repository" "franchise_api" {
  name                 = "franchise-api"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = var.environment
    Project     = "franchise-api"
    ManagedBy   = "terraform"
  }
}

# ── IAM: App Runner acceso a ECR ─────────────────────────────────────────────

resource "aws_iam_role" "apprunner_ecr_role" {
  name = "apprunner-ecr-access-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "build.apprunner.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })

  tags = {
    Project   = "franchise-api"
    ManagedBy = "terraform"
  }
}

resource "aws_iam_role_policy_attachment" "apprunner_ecr_policy" {
  role       = aws_iam_role.apprunner_ecr_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess"
}

# ── IAM: App Runner acceso a DynamoDB ────────────────────────────────────────

resource "aws_iam_role" "apprunner_instance_role" {
  name = "apprunner-instance-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "tasks.apprunner.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })

  tags = {
    Project   = "franchise-api"
    ManagedBy = "terraform"
  }
}

resource "aws_iam_role_policy_attachment" "apprunner_dynamodb_policy" {
  role       = aws_iam_role.apprunner_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
}

# ── App Runner ────────────────────────────────────────────────────────────────

resource "aws_apprunner_service" "franchise_api" {
  service_name = "franchise-api"

  source_configuration {
    authentication_configuration {
      access_role_arn = aws_iam_role.apprunner_ecr_role.arn
    }
    image_repository {
      image_identifier      = "${aws_ecr_repository.franchise_api.repository_url}:latest"
      image_repository_type = "ECR"
      image_configuration {
        port = "8080"
        runtime_environment_variables = {
          AWS_REGION          = var.aws_region
          DYNAMODB_TABLE_NAME = var.table_name
        }
      }
    }
    auto_deployments_enabled = false
  }

  instance_configuration {
    instance_role_arn = aws_iam_role.apprunner_instance_role.arn
    cpu               = "256"
    memory            = "512"
  }

  tags = {
    Environment = var.environment
    Project     = "franchise-api"
    ManagedBy   = "terraform"
  }

  depends_on = [
    aws_iam_role_policy_attachment.apprunner_ecr_policy,
    aws_iam_role_policy_attachment.apprunner_dynamodb_policy
  ]
}
