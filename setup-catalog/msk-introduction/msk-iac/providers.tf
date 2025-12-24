terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = { source = "hashicorp/aws" }
  }
}


provider "aws" {
  region = var.aws_region
}