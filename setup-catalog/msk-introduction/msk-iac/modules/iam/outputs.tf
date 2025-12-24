output "instance_profile" { value = aws_iam_instance_profile.ec2_profile.name }
output "ec2_role_arn" { value = aws_iam_role.ec2_role.arn }


