# Case 1: Infrastructure Protection
## Objectives:
- How to configure patching for EC2 instances - using SSM Patch Manager
- Create a mechanism to ensure that your instances are periodically updated - using AWS Systems Manager Maintenance Windows
- Implement reporting mechanism for patching compliance in order to: - using AWS Systems Manager Compliance
    - ensure your instances are patched
    - updated instances must be compliant with the defined security policy

## What need to be done?
1. Configure SSM patch manager to run periodically during the pre-approved maintenance window.
2. Configure AWS Config rule to report patching compliance.

# Case 2: Data Protection
## Objectives:
- How to tag your Amazon Relational Database Service (Amazon RDS) as Restricted.
- Modify an AWS Key Management Service (KMS)  Customer Managed Key (CMK) policy to be more restrictive.

## What need to be done?
1. Tag your RDS instances with the tag key: DataClassification and the tag value: public, internal or restricted.
2. Configure a KMS Customer Managed Key (CMK) with a key policy that shall be limited to the authorized service and resource
3. Use the Key to encrypt/decrypt RDS instances.
4. Configure an AWS Config rule to validate if the RDS instance has been tagged with data classification key: required-tags

# Case 3: Security Assurance
## Objectives:
- Setup Logging at OS Level using Amazon CloudWatch.
- Setup notifications concerning relevant OS events.

## What need to be done?
1. When an EC2 instance is launched, it should automatically install and configure the CloudWatch agent to send OS level logs to CloudWatch Logs - using User Data script.
2. Check the corresponding CloudWatch log group to ensure that logs are being sent.
3. Create a CloudWatch metric filter and alarm to notify when specific OS events occur - using CloudWatch Alarms and SNS.

# Case 4: Identity and Access Management
## Objectives:
- Configure your IAM policies to prevent unauthorized traffic mirroring of your VPC network.
- Deploy an AWS Config custom rule to detect traffic mirroring on the VPC.

## Problem summary:
- Devs use traffic mirroring to monitor network traffic in their VPCs without security team knowing

## What need to be done?
- Configure the IAM policies to prevent unauthorized traffic mirroring of your VPC network.
- Implement an AWS Config custom rule to detect traffic mirroring on the VPC (since there is no managed rule for this purpose) --> help security team to be alerted