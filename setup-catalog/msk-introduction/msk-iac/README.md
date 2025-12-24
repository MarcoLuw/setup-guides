# MSK Terraform Deployment Guide

This Terraform configuration deploys an **AWS Managed Streaming for Apache Kafka (MSK)** cluster with VPC, security groups, IAM roles, and a bastion host for testing.

## Architecture Overview

- **VPC Module**: Creates a VPC with public and private subnets across multiple availability zones
- **MSK Module**: Deploys a Kafka cluster in private subnets with TLS/IAM authentication
- **IAM Module**: Creates instance profile, role, and policies for MSK client access
- **Security Groups**: Manages inbound/outbound rules for MSK brokers and client instances
- **Bastion Module**: Provides a jump host in public subnet for testing Kafka connectivity

## Prerequisites

1. **AWS Account** with appropriate permissions (EC2, MSK, VPC, IAM)
2. **Terraform** >= 1.0
3. **SSH Key Pair** - Generate or provide:
   ```bash
   ssh-keygen -t rsa -b 2048 -f key1 -N ""
   # This creates key1 (private) and key1.pub (public)
   # Place key1.pub in the MSK directory
   ```
4. **AWS CLI** configured with credentials
5. **Kafka CLI Tools** (optional, for testing cluster connectivity)

## Project Structure

```
MSK/
├── main.tf              # Main configuration with modules and resources
├── variables.tf         # Variable definitions
├── outputs.tf          # Output definitions
├── terraform.tfvars    # Variable values (create from template)
├── key1.pub            # SSH public key for bastion access
├── modules/
│   ├── vpc/            # VPC, subnets, route tables
│   ├── msk/            # MSK cluster definition
│   ├── iam/            # IAM roles and policies
│   └── bastion/        # Bastion EC2 instance
└── README.md           # This file
```

## Variables

Create a `terraform.tfvars` file with your values:

```hcl
aws_region             = "us-east-1"
vpc_cidr               = "10.0.0.0/16"
public_subnet_cidrs    = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
private_subnet_cidrs   = ["10.0.11.0/24", "10.0.12.0/24", "10.0.13.0/24"]
azs                    = ["us-east-1a", "us-east-1b", "us-east-1c"]
allowed_ssh_cidr       = "0.0.0.0/0"  # Restrict to your IP in production
msk_cluster_name       = "my-kafka-cluster"
kafka_version          = "3.6.0"
broker_instance_type   = "kafka.m5.large"
ebs_gb                 = 1000
key_name               = "my-key"
```

### Key Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `aws_region` | AWS region | `us-east-1` |
| `vpc_cidr` | VPC CIDR block | `10.0.0.0/16` |
| `msk_cluster_name` | MSK cluster name | Required |
| `kafka_version` | Kafka version (e.g., 3.6.0) | Required |
| `broker_instance_type` | EC2 instance type for brokers | `kafka.m5.large` |
| `ebs_gb` | EBS volume size per broker (GB) | `1000` |
| `key_name` | SSH key pair name | Required |
| `allowed_ssh_cidr` | CIDR block allowed SSH access | `0.0.0.0/0` |

## Deployment Steps

### 1. Initialize Terraform

```bash
cd MSK
terraform init
```

### 2. Validate Configuration

```bash
terraform validate
terraform fmt
```

### 3. Plan Deployment

```bash
terraform plan -out=tfplan
```

Review the plan to ensure resources match your expectations.

### 4. Apply Configuration

```bash
terraform apply tfplan
```

This will:
- Create the VPC and subnets
- Deploy the MSK cluster (takes ~10-15 minutes)
- Set up security groups and IAM roles
- Launch a bastion host

### 5. Retrieve Outputs

After deployment completes:

```bash
terraform output
```

Key outputs:
- **bastion_public_ip**: IP address of the bastion host
- **msk_broker_nodes**: MSK broker endpoints
- **msk_zookeeper_connect**: Zookeeper connection string

## Testing MSK Connectivity

### 1. SSH into Bastion

```bash
ssh -i key1 ec2-user@<bastion_public_ip>
```

### 2. Verify IAM Authentication

The bastion instance has an IAM instance profile with MSK permissions. Test:

```bash
# On the bastion host
aws kafka describe-cluster --cluster-arn $(aws kafka list-clusters --query 'ClusterInfoList[0].ClusterArn' --output text)
```

### 3. Produce/Consume Messages (with Kafka CLI)

If Kafka CLI tools are installed on the bastion:

```bash
# Get broker endpoints
BROKERS=$(aws kafka get-bootstrap-brokers --cluster-arn <cluster-arn> --query 'BootstrapBrokerStringTls' --output text)

# Produce a message
echo "Hello Kafka" | kafka-console-producer.sh \
  --broker-list $BROKERS \
  --topic test-topic \
  --producer-property security.protocol=TLS

# Consume messages
kafka-console-consumer.sh \
  --bootstrap-servers $BROKERS \
  --topic test-topic \
  --from-beginning \
  --consumer-property security.protocol=TLS
```

## Security Notes

- **TLS/IAM Authentication**: MSK is configured with TLS and IAM-based access control
- **Private Subnets**: MSK brokers run only in private subnets with no public endpoints
- **Bastion Access**: Use the bastion host as a jump point to interact with MSK
- **SSH CIDR**: Restrict `allowed_ssh_cidr` to your IP in production (not `0.0.0.0/0`)
- **IAM Policies**: The IAM module grants minimal required permissions for client operations

## Cost Optimization

- **Broker Type**: `kafka.m5.large` is suitable for testing; use smaller types for cost reduction
- **EBS Storage**: Adjust `ebs_gb` based on message retention needs
- **Multi-AZ**: Current setup uses 3 AZs; reduce to 2 for lower cost (edit `azs` and subnet counts)

## Cleanup

### Destroy Only MSK Module

```bash
terraform destroy -target=module.msk
```

### Destroy Everything

```bash
terraform destroy
```

When prompted, confirm with `yes`.

**Warning**: Destroying MSK will delete all data. Back up critical topics beforehand.

## Troubleshooting

### MSK Deployment Takes Too Long

- MSK clusters typically take 10-15 minutes to become available
- Check AWS CloudFormation events in the console for detailed progress

### Bastion Cannot Connect to MSK

1. Verify security group rules allow port 9098 (TLS) from clients_sg to msk_sg
2. Confirm bastion has the correct IAM instance profile
3. Check MSK cluster is in "Active" state

### Terraform State Issues

If state becomes corrupted:

```bash
terraform refresh
terraform state list  # View all resources
```

## Module Details

### VPC Module
- Creates VPC with customizable CIDR
- Deploys public and private subnets across AZs
- Configures NAT gateways for private subnet egress

### MSK Module
- Deploys Kafka cluster with TLS/IAM authentication
- Configures CloudWatch Logs and S3 logging
- Supports custom broker properties

### IAM Module
- Creates instance profile for MSK client applications
- Grants `kafka-cluster:*` permissions
- Allows topic and consumer group management

### Bastion Module
- Launches Amazon Linux 2 EC2 instance
- Includes AWS CLI and Kafka CLI tools
- Attached to MSK client security group for connectivity

## Additional Resources

- [AWS MSK Documentation](https://docs.aws.amazon.com/msk/)
- [Kafka Official Docs](https://kafka.apache.org/documentation/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)

## Support & Issues

For issues:
1. Check `terraform plan` output for validation errors
2. Review AWS CloudFormation events in the console
3. Verify IAM permissions and security group rules
4. Check Terraform logs: `TF_LOG=DEBUG terraform apply`

---

**Last Updated**: December 2025
