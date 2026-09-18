resource "aws_iam_user" "expense_flow_presign" {
  name = var.iam_user_name
}

data "aws_iam_policy_document" "expense_flow_presign" {
  statement {
    sid    = "AllowObjectReadWrite"
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:GetObject",
    ]
    resources = ["${aws_s3_bucket.expense_flow.arn}/*"]
  }

  statement {
    sid    = "AllowBucketLocation"
    effect = "Allow"
    actions = [
      "s3:GetBucketLocation",
    ]
    resources = [aws_s3_bucket.expense_flow.arn]
  }
}

resource "aws_iam_user_policy" "expense_flow_presign" {
  name   = "${var.iam_user_name}-presign-policy"
  user   = aws_iam_user.expense_flow_presign.name
  policy = data.aws_iam_policy_document.expense_flow_presign.json
}

resource "aws_iam_access_key" "expense_flow_presign" {
  user = aws_iam_user.expense_flow_presign.name
}
