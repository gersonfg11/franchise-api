variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "us-east-1"
}

variable "table_name" {
  description = "DynamoDB table name"
  type        = string
  default     = "franchises"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "production"
}
