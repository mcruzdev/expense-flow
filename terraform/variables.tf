variable "aws_region" {
  description = "AWS region to deploy resources into"
  type        = string
  default     = "us-east-1"
}

variable "bucket_name" {
  description = "Name of the S3 bucket"
  type        = string
  default     = "guru-quarkus-expense-flow"
}

variable "cors_allowed_origins" {
  description = "Origins allowed to PUT objects directly to S3 (e.g. your app's URL)"
  type        = list(string)
  default     = ["http://localhost:8080"]
}

variable "iam_user_name" {
  description = "Name of the IAM user used by the app to generate pre-signed URLs"
  type        = string
  default     = "expense-flow-presign"
}
