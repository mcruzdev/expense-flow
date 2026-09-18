output "bucket_name" {
  description = "Name of the created S3 bucket"
  value       = aws_s3_bucket.expense_flow.id
}

output "bucket_arn" {
  description = "ARN of the created S3 bucket"
  value       = aws_s3_bucket.expense_flow.arn
}

output "presign_access_key_id" {
  description = "Access key ID for the pre-signed URL IAM user"
  value       = aws_iam_access_key.expense_flow_presign.id
}

output "presign_secret_access_key" {
  description = "Secret access key for the pre-signed URL IAM user"
  value       = aws_iam_access_key.expense_flow_presign.secret
  sensitive   = true
}
