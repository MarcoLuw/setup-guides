data "aws_iam_policy_document" "ec2_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}


resource "aws_iam_role" "ec2_role" {
  name               = "msk-ec2-role"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume_role.json
}


resource "aws_iam_policy" "msk_client_policy" {
  name = "msk-client-policy"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Action = [
        "kafka:*",
        "kafka-cluster:*"
      ],
      Resource = "*"
    }]
  })
}


resource "aws_iam_role_policy_attachment" "attach_client_policy" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.msk_client_policy.arn
}


resource "aws_iam_instance_profile" "ec2_profile" {
  name = "msk-ec2-profile"
  role = aws_iam_role.ec2_role.name
}