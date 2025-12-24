data "aws_caller_identity" "me" {}


module "vpc" {
  source               = "./modules/vpc"
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones   = var.azs
}


# Security group for clients
resource "aws_security_group" "clients_sg" {
  name        = "msk-clients-sg"
  description = "Clients allowed to talk to MSK and bastion SSH"
  vpc_id      = module.vpc.vpc_id


  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
  }


  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}


# Security group for MSK brokers
resource "aws_security_group" "msk_sg" {
  name        = "msk-brokers-sg"
  description = "MSK brokers security group"
  vpc_id      = module.vpc.vpc_id
}


# allow TLS/IAM port from clients to MSK
resource "aws_security_group_rule" "msk_tls_from_clients" {
  type                     = "ingress"
  from_port                = 9098
  to_port                  = 9098
  protocol                 = "tcp"
  security_group_id        = aws_security_group.msk_sg.id
  source_security_group_id = aws_security_group.clients_sg.id
  description              = "Allow TLS/IAM from clients"
}


# IAM module (creates instance profile + role + policy for clients)
module "iam" {
  source           = "./modules/iam"
  msk_cluster_name = var.msk_cluster_name
  region           = var.aws_region
  account_id       = data.aws_caller_identity.me.account_id
}


#MSK module
module "msk" {
  source               = "./modules/msk"
  cluster_name         = var.msk_cluster_name
  kafka_version        = var.kafka_version
  broker_instance_type = var.broker_instance_type
  ebs_gb               = var.ebs_gb
  subnets              = module.vpc.private_subnets
  security_group_id    = aws_security_group.msk_sg.id
}


# Bastion / Test instance
module "bastion" {
  source            = "./modules/bastion"
  subnet_id         = module.vpc.public_subnets[0]
  security_group_id = aws_security_group.clients_sg.id
  key_name          = var.key_name
  instance_profile  = module.iam.instance_profile
}
#key pair
resource "aws_key_pair" "key1" {
  key_name   = var.key_name
  public_key = file("key1.pub")
}
