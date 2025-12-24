# Terraform Infrastructure

This directory contains the Terraform configurations for the EKS SaaS Workshop.

## Prerequisites

- Terraform >= 1.9.0
- AWS CLI configured with appropriate credentials
- Sufficient AWS permissions to create resources

## Structure

- **environments/**: Environment-specific configurations
  - **bootstrap/**: S3 bucket creation for Terraform state (optional)
  - **dev/**: Development environment configuration
- **modules/**: Reusable Terraform modules
  - **iam/**: IAM roles and policies
  - **ide/**: IDE infrastructure (EC2, CloudFront, Lambda, etc.)
  - **codebuild/**: CodeBuild projects and automation
  - **eks-orchestrator/**: EKS cluster and orchestration
- **scripts/**: Helper scripts for deployment and management

### Directory Structure
```bash
terraform/
│
├── environments/
│   ├── bootstrap/                          # OPTIONAL: Remove if you create bucket manually
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   │
│   └── dev/
│       ├── main.tf
│       ├── backend.tf                      # Hardcoded bucket name
│       ├── variables.tf
│       ├── outputs.tf
│       ├── terraform.tfvars.example
│       └── versions.tf
│
├── modules/
│   ├── iam/                                # ✅ SELF-IMPLEMENT
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   └── policies/
│   │       └── shared-role-policy.json
│   │
│   ├── ide/                                # ✅ SELF-IMPLEMENT
│   │   ├── main.tf
│   │   ├── cloudfront.tf
│   │   ├── secrets.tf
│   │   ├── lambda.tf
│   │   ├── ssm.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   ├── templates/
│   │   │   ├── user-data.sh.tpl
│   │   │   ├── caddy-config.tpl
│   │   │   └── gitea-compose.yml.tpl
│   │   └── lambda/
│   │       ├── bootstrap-trigger/
│   │       │   └── index.py
│   │       └── password-exporter/
│   │           └── index.py
│   │
│   ├── codebuild/                          # ✅ SELF-IMPLEMENT
│   │   ├── main.tf
│   │   ├── lambda.tf
│   │   ├── eventbridge.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   ├── templates/
│   │   │   └── buildspec.yml.tpl
│   │   └── lambda/
│   │       ├── start-build/
│   │       │   └── index.js
│   │       └── report-build/
│   │           └── index.js
│   │
│   └── eks-orchestrator/                   # ✅ SELF-IMPLEMENT
│       ├── main.tf
│       ├── access-entries.tf
│       ├── variables.tf
│       ├── outputs.tf
│       └── data.tf
│
├── scripts/
│   ├── deploy.sh
│   ├── destroy.sh
│   └── ssh-to-ide.sh
│
├── .gitignore
├── .terraform-version
├── Makefile
└── README.md
```

## Configuration values

For convenience, you can find all configuration values used in the Terraform codebase extracted from Cloudformation in the [configuration.md](../configuration.md) file.

## Usage

### Bootstrap (Optional)

If you need to create the S3 bucket for Terraform state:

```bash
cd environments/bootstrap
terraform init
terraform apply
```

### Deploy Development Environment

```bash
cd environments/dev
terraform init
terraform plan
terraform apply
```

Or use the Makefile:

```bash
make init
make plan
make apply
```

## Modules

Each module is marked with ✅ SELF-IMPLEMENT and requires implementation based on your specific requirements.