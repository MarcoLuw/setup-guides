data "aws_ssm_parameter" "ubuntu_24" {
  name = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}


resource "aws_instance" "bastion" {
  ami                         = data.aws_ssm_parameter.ubuntu_24.value
  instance_type               = "t3.medium"
  subnet_id                   = var.subnet_id
  key_name                    = var.key_name
  vpc_security_group_ids      = [var.security_group_id]
  iam_instance_profile        = var.instance_profile
  associate_public_ip_address = true
  tags                        = { Name = "msk-bastion" }


  user_data = <<-EOF
                #!/bin/bash
                set -euo pipefail
                exec > /var/log/bastion-user-data.log 2>&1
                date
                echo "starting bastion user-data"
                export DEBIAN_FRONTEND=noninteractive

                apt-get update -y
                apt-get install -y wget curl ca-certificates gnupg lsb-release || true

                # install Java 17 (use OpenJDK as fallback if Corretto package not available)
                if ! apt-get install -y java-17-amazon-corretto-headless; then
                  apt-get install -y openjdk-17-jre-headless || apt-get install -y default-jre || true
                fi

                cd /opt
                wget -q https://archive.apache.org/dist/kafka/3.7.0/kafka_2.13-3.7.0.tgz
                tar xzf kafka_2.13-3.7.0.tgz
                wget -q https://github.com/aws/aws-msk-iam-auth/releases/download/v2.3.5/aws-msk-iam-auth-2.3.5-all.jar
                mv -f aws-msk-iam-auth-2.3.5-all.jar /opt/kafka_2.13-3.7.0/libs/ || true

                # create client.properties file in kafka config directory (use single-quoted heredoc)
                cat > /opt/kafka_2.13-3.7.0/client.properties <<'EOT'
                security.protocol=SASL_SSL
                sasl.mechanism=AWS_MSK_IAM
                sasl.jaas.config=software.amazon.msk.auth.iam.IAMLoginModule required;
                sasl.client.callback.handler.class=software.amazon.msk.auth.iam.IAMClientCallbackHandler
                EOT

                chown -R ubuntu:ubuntu /opt/kafka_2.13-3.7.0 || true
                echo "bastion user-data finished"
                EOF
}


