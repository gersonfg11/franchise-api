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

# ── IAM: Rol para EC2 (acceso a ECR + DynamoDB) ───────────────────────────────

resource "aws_iam_role" "ec2_role" {
  name = "franchise-api-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })

  tags = {
    Project   = "franchise-api"
    ManagedBy = "terraform"
  }
}

resource "aws_iam_role_policy_attachment" "ec2_ecr_policy" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_role_policy_attachment" "ec2_dynamodb_policy" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "franchise-api-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# ── Security Group ────────────────────────────────────────────────────────────

resource "aws_security_group" "franchise_api" {
  name        = "franchise-api-sg"
  description = "Allow HTTP on 8080"

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Project   = "franchise-api"
    ManagedBy = "terraform"
  }
}

# ── AMI más reciente de Amazon Linux 2023 ─────────────────────────────────────

data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

# ── EC2 t3.micro (free tier eligible) ────────────────────────────────────────

resource "aws_instance" "franchise_api" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = "t3.micro"
  iam_instance_profile        = aws_iam_instance_profile.ec2_profile.name
  vpc_security_group_ids      = [aws_security_group.franchise_api.id]
  associate_public_ip_address = true

  user_data = <<-EOF
    #!/bin/bash
    yum update -y
    yum install -y docker
    systemctl start docker
    systemctl enable docker

    aws ecr get-login-password --region ${var.aws_region} | \
      docker login --username AWS --password-stdin ${aws_ecr_repository.franchise_api.repository_url}

    docker pull ${aws_ecr_repository.franchise_api.repository_url}:latest

    docker run -d \
      -p 8080:8080 \
      -e AWS_REGION=${var.aws_region} \
      -e DYNAMODB_TABLE_NAME=${var.table_name} \
      --name franchise-api \
      --restart always \
      ${aws_ecr_repository.franchise_api.repository_url}:latest
  EOF

  tags = {
    Name        = "franchise-api"
    Environment = var.environment
    Project     = "franchise-api"
    ManagedBy   = "terraform"
  }

  depends_on = [
    aws_iam_role_policy_attachment.ec2_ecr_policy,
    aws_iam_role_policy_attachment.ec2_dynamodb_policy
  ]
}
