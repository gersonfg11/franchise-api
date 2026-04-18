output "dynamodb_table_name" {
  description = "Name of the DynamoDB table"
  value       = aws_dynamodb_table.franchises.name
}

output "dynamodb_table_arn" {
  description = "ARN of the DynamoDB table"
  value       = aws_dynamodb_table.franchises.arn
}

output "ecr_repository_url" {
  description = "ECR repository URL"
  value       = aws_ecr_repository.franchise_api.repository_url
}

output "apprunner_service_url" {
  description = "Public URL of the App Runner service"
  value       = "https://${aws_apprunner_service.franchise_api.service_url}"
}
