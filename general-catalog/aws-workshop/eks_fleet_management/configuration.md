# Extracted Values from CloudFormation - Organized by Module

<details>
<summary><b>MODULE: networking (VPC, Subnets, Routing)</b></summary>

## Network Configuration

```yaml
VPC:
  CIDR: "192.168.0.0/16"
  EnableDnsHostnames: true
  EnableDnsSupport: true
  Tags:
    Name: "fleet-workshop/IDE-Fleet/IdeVPC"

PublicSubnet:
  CIDR: "192.168.0.0/24"
  MapPublicIpOnLaunch: true
  AvailabilityZone: "Select first AZ"
  Tags:
    Name: "fleet-workshop/IDE-Fleet/IdeVPC/PublicSubnet1"
    aws-cdk:subnet-name: "Public"
    aws-cdk:subnet-type: "Public"

InternetGateway:
  Attached: true
  Tags:
    Name: "fleet-workshop/IDE-Fleet/IdeVPC"

RouteTable:
  Route:
    DestinationCidrBlock: "0.0.0.0/0"
    Gateway: "InternetGateway"
  Tags:
    Name: "fleet-workshop/IDE-Fleet/IdeVPC/PublicSubnet1"

RouteTableAssociation:
  RouteTableId: "Reference to RouteTable"
  SubnetId: "Reference to PublicSubnet"
```

</details>

<details>
<summary><b>MODULE: networking (Security Groups)</b></summary>

## Security Groups

```yaml
IDESecurityGroup:
  GroupDescription: "IDE security group"
  VpcId: "Reference to VPC"
  
  IngressRules:
    - Description: "Gitea API from VPC"
      FromPort: 9999
      ToPort: 9999
      Protocol: "tcp"
      CidrBlocks: ["VPC CIDR Block"]
    
    - Description: "Gitea SSH from VPC"
      FromPort: 2222
      ToPort: 2222
      Protocol: "tcp"
      CidrBlocks: ["VPC CIDR Block"]
    
    - Description: "HTTP from CloudFront only"
      FromPort: 80
      ToPort: 80
      Protocol: "tcp"
      SourcePrefixListId: "com.amazonaws.global.cloudfront.origin-facing"
      # Get this using Lambda or data source
  
  EgressRules:
    - Description: "Allow all outbound traffic by default"
      CidrBlock: "0.0.0.0/0"
      Protocol: "-1"
```

</details>

<details>
<summary><b>MODULE: iam (Shared Role for EC2/CodeBuild)</b></summary>

## SharedRole - Used by EC2, CodeBuild, Glue

```yaml
SharedRole:
  RoleName: "Auto-generated or specify"
  
  AssumeRolePolicyDocument:
    Version: "2012-10-17"
    Statement:
      - Effect: "Allow"
        Principal:
          Service:
            - "codebuild.amazonaws.com"
            - "ec2.amazonaws.com"
            - "glue.amazonaws.com"
        Action: "sts:AssumeRole"
  
  ManagedPolicyArns:
    - "arn:aws:iam::aws:policy/AdministratorAccess"
    - "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
  
  InlinePolicy:
    PolicyName: "SharedRoleDefaultPolicy"
    PolicyDocument:
      Version: "2012-10-17"
      Statement:
        - Effect: "Allow"
          Action:
            - "logs:CreateLogStream"
            - "logs:PutLogEvents"
          Resource: "IDELogGroup ARN"
        
        - Effect: "Allow"
          Action:
            - "secretsmanager:DescribeSecret"
            - "secretsmanager:GetSecretValue"
          Resource: "IDEPasswordSecret ARN"
        
        - Effect: "Allow"
          Action:
            - "logs:CreateLogGroup"
            - "logs:CreateLogStream"
            - "logs:PutLogEvents"
          Resource:
            - "arn:aws:logs:REGION:ACCOUNT_ID:log-group:/aws/codebuild/PROJECT_NAME:*"
            - "arn:aws:logs:REGION:ACCOUNT_ID:log-group:/aws/codebuild/PROJECT_NAME"
            # Repeat for each CodeBuild project
        
        - Effect: "Allow"
          Action:
            - "codebuild:BatchPutCodeCoverages"
            - "codebuild:BatchPutTestCases"
            - "codebuild:CreateReport"
            - "codebuild:CreateReportGroup"
            - "codebuild:UpdateReport"
          Resource:
            - "arn:aws:codebuild:REGION:ACCOUNT_ID:report-group/PROJECT_NAME-*"
            # Repeat for each CodeBuild project

InstanceProfile:
  Roles:
    - "SharedRole"
```

</details>

<details>
<summary><b>MODULE: iam (Lambda Roles)</b></summary>

## Lambda IAM Roles

```yaml
PrefixListLookupRole:
  AssumeRolePolicyDocument:
    Version: "2012-10-17"
    Statement:
      - Effect: "Allow"
        Principal:
          Service: "lambda.amazonaws.com"
        Action: "sts:AssumeRole"
  
  ManagedPolicyArns:
    - "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  
  InlinePolicy:
    PolicyName: "PrefixListLookupPolicy"
    PolicyDocument:
      Statement:
        - Effect: "Allow"
          Action: "ec2:DescribeManagedPrefixLists"
          Resource: "*"

BootstrapFunctionRole:
  AssumeRolePolicyDocument:
    Version: "2012-10-17"
    Statement:
      - Effect: "Allow"
        Principal:
          Service: "lambda.amazonaws.com"
        Action: "sts:AssumeRole"
  
  ManagedPolicyArns:
    - "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  
  InlinePolicy:
    PolicyName: "BootstrapPolicy"
    PolicyDocument:
      Statement:
        - Effect: "Allow"
          Action: "iam:PassRole"
          Resource: "SharedRole ARN"
        
        - Effect: "Allow"
          Action:
            - "ec2:DescribeInstances"
            - "iam:ListInstanceProfiles"
            - "ssm:DescribeInstanceInformation"
            - "ssm:GetCommandInvocation"
            - "ssm:SendCommand"
          Resource: "*"

PasswordExporterRole:
  AssumeRolePolicyDocument:
    Version: "2012-10-17"
    Statement:
      - Effect: "Allow"
        Principal:
          Service: "lambda.amazonaws.com"
        Action: "sts:AssumeRole"
  
  ManagedPolicyArns:
    - "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  
  InlinePolicy:
    PolicyName: "PasswordExporterPolicy"
    PolicyDocument:
      Statement:
        - Effect: "Allow"
          Action:
            - "secretsmanager:DescribeSecret"
            - "secretsmanager:GetSecretValue"
          Resource: "IDEPasswordSecret ARN"

StartBuildRole:
  AssumeRolePolicyDocument:
    Version: "2012-10-17"
    Statement:
      - Effect: "Allow"
        Principal:
          Service: "lambda.amazonaws.com"
        Action: "sts:AssumeRole"
  
  ManagedPolicyArns:
    - "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  
  InlinePolicy:
    PolicyName: "StartBuildPolicy"
    PolicyDocument:
      Statement:
        - Effect: "Allow"
          Action: "codebuild:StartBuild"
          Resource: "CodeBuild Project ARN"

ReportBuildRole:
  AssumeRolePolicyDocument:
    Version: "2012-10-17"
    Statement:
      - Effect: "Allow"
        Principal:
          Service: "lambda.amazonaws.com"
        Action: "sts:AssumeRole"
  
  ManagedPolicyArns:
    - "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  
  InlinePolicy:
    PolicyName: "ReportBuildPolicy"
    PolicyDocument:
      Statement:
        - Effect: "Allow"
          Action:
            - "codebuild:BatchGetBuilds"
            - "codebuild:ListBuildsForProject"
          Resource: "CodeBuild Project ARN"
```

</details>

<details>
<summary><b>MODULE: s3-backend (Terraform State Bucket)</b></summary>

## S3 Bucket for Terraform State

```yaml
TFStateBackendBucket:
  BucketName: "Auto-generated or specify"
  
  VersioningConfiguration:
    Status: "Enabled"
  
  DeletionPolicy: "Retain"
  UpdateReplacePolicy: "Retain"
  
  # Note: No encryption explicitly defined in CFN
  # But Terraform should add encryption
```

</details>

<details>
<summary><b>MODULE: ide (EC2 Instance)</b></summary>

## EC2 Instance Configuration

```yaml
IDEInstance:
  InstanceType: "t3.medium"
  
  ImageId:
    SSMParameter: "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-6.1-x86_64"
  
  AvailabilityZone: "Select first AZ from region"
  
  BlockDeviceMappings:
    - DeviceName: "/dev/xvda"
      Ebs:
        VolumeSize: 30
        VolumeType: "gp3"
        Encrypted: true
        DeleteOnTermination: true
  
  NetworkInterfaces:
    - AssociatePublicIpAddress: true
      DeviceIndex: "0"
      GroupSet: ["IDESecurityGroup ID"]
      SubnetId: "PublicSubnet ID"
  
  IamInstanceProfile: "InstanceProfile for SharedRole"
  
  UserData: "#!/bin/bash"  # Minimal - actual bootstrap via SSM
  
  Tags:
    Name: "fleet-workshop/IDE-Fleet/IDE-Fleet"
  
  DependsOn:
    - "InternetGateway"
    - "PublicSubnetRoute"
    - "SharedRole"
```

</details>

<details>
<summary><b>MODULE: ide (Secrets Manager)</b></summary>

## Secrets Manager - IDE Password

```yaml
IDEPasswordSecret:
  Name: "Auto-generated"
  
  GenerateSecretString:
    ExcludeCharacters: '"@/\'
    ExcludePunctuation: true
    GenerateStringKey: "password"
    IncludeSpace: false
    PasswordLength: 32
    SecretStringTemplate: '{"password":""}'
  
  DeletionPolicy: "Delete"
  UpdateReplacePolicy: "Delete"
```

</details>

<details>
<summary><b>MODULE: ide (CloudFront Distribution)</b></summary>

## CloudFront Distribution

```yaml
IDEDistribution:
  Enabled: true
  HttpVersion: "http2"
  IPV6Enabled: true
  
  Origins:
    - OriginId: "ide-ec2-origin"
      DomainName: "EC2 Instance Public DNS"
      CustomOriginConfig:
        HTTPPort: 80
        HTTPSPort: 443
        OriginProtocolPolicy: "http-only"
        OriginSSLProtocols: ["TLSv1.2"]
  
  DefaultCacheBehavior:
    TargetOriginId: "ide-ec2-origin"
    ViewerProtocolPolicy: "allow-all"
    AllowedMethods:
      - "GET"
      - "HEAD"
      - "OPTIONS"
      - "PUT"
      - "PATCH"
      - "POST"
      - "DELETE"
    CachedMethods:
      - "GET"
      - "HEAD"
      - "OPTIONS"
    Compress: true
    CachePolicyId: "4135ea2d-6df8-44a3-9df3-4b5a84be39ad"  # CachingDisabled
    OriginRequestPolicyId: "216adef6-5c7f-47e4-b989-5492eafa07d3"  # AllViewer
  
  Restrictions:
    GeoRestriction:
      RestrictionType: "none"
  
  ViewerCertificate:
    CloudFrontDefaultCertificate: true
```

</details>

<details>
<summary><b>MODULE: ide (CloudWatch Logs)</b></summary>

## CloudWatch Log Group

```yaml
IDELogGroup:
  LogGroupName: "Auto-generated or specify"
  RetentionInDays: 7
  DeletionPolicy: "Retain"
  UpdateReplacePolicy: "Retain"
```

</details>

<details>
<summary><b>MODULE: ide (Lambda - Prefix List Lookup)</b></summary>

## Lambda Function: CloudFront Prefix List Lookup

```python
# lambda/prefix-list-lookup/index.py
from __future__ import print_function
import boto3
import traceback
import cfnresponse

def lambda_handler(event, context):
    print('Event: {}'.format(event))
    print('context: {}'.format(context))
    responseData = {}

    status = cfnresponse.SUCCESS

    if event['RequestType'] == 'Delete':
        responseData = {'Success': 'Custom Resource removed'}
        cfnresponse.send(event, context, status, responseData, 'CustomResourcePhysicalID')
    else:
        try:
            # Open AWS clients
            ec2 = boto3.client('ec2')

            res = ec2.describe_managed_prefix_lists(
               Filters=[{
                  'Name': 'prefix-list-name',
                  'Values': ['com.amazonaws.global.cloudfront.origin-facing']
               }]
            )

            responseData = {'PrefixListId': str(res['PrefixLists'][0]['PrefixListId'])}
        except Exception as e:
            status = cfnresponse.FAILED
            tb_err = traceback.format_exc()
            print(tb_err)
            responseData = {'Error': tb_err}
        finally:
            cfnresponse.send(event, context, status, responseData, 'CustomResourcePhysicalID')
```

```yaml
Lambda_PrefixListLookup:
  FunctionName: "Auto-generated"
  Runtime: "python3.12"
  Handler: "index.lambda_handler"
  Timeout: 180
  Role: "PrefixListLookupRole ARN"
  Code: "Inline code above"
```

**Note for Terraform:** You can use `aws_ec2_managed_prefix_list` data source instead of this Lambda.

</details>

<details>
<summary><b>MODULE: ide (Lambda - Bootstrap Trigger)</b></summary>

## Lambda Function: Bootstrap EC2 Instance

```python
# lambda/bootstrap-trigger/index.py
from __future__ import print_function
import boto3
import json
import os
import time
import traceback
import cfnresponse
from botocore.exceptions import WaiterError

def lambda_handler(event, context):
    print('Event: {}'.format(event))
    print('context: {}'.format(context))
    responseData = {}

    status = cfnresponse.SUCCESS

    if event['RequestType'] == 'Delete':
        responseData = {'Success': 'Custom Resource removed'}
        cfnresponse.send(event, context, status, responseData, 'CustomResourcePhysicalID')
    else:
        try:
            # Open AWS clients
            ec2 = boto3.client('ec2')
            ssm = boto3.client('ssm')

            instance_id = event['ResourceProperties']['InstanceId']

            print('Waiting for the instance to be ready...')
            # Wait for Instance to become ready
            instance_state = 'unknown'
            print('Instance is currently in state'.format(instance_state))
            while instance_state != 'running':
                time.sleep(5)
                di = ec2.describe_instances(InstanceIds=[instance_id])
                instance_state = di['Reservations'][0]['Instances'][0]['State']['Name']
                print('Waiting for instance in state: {}'.format(instance_state))

            print('Instance is ready')

            print('Waiting for instance to come online in SSM...')
            for i in range(1, 60):
              response = ssm.describe_instance_information(Filters=[{'Key': 'InstanceIds', 'Values': [instance_id]}])
              if len(response["InstanceInformationList"]) == 0:
                print('No instances in SSM')
              elif len(response["InstanceInformationList"]) > 0 and \
                    response["InstanceInformationList"][0]["PingStatus"] == "Online" and \
                    response["InstanceInformationList"][0]["InstanceId"] == instance_id:
                print('Instance is online in SSM')
                break
              time.sleep(10)

            ssm_document = event['ResourceProperties']['SsmDocument']

            ssm.send_command(
                InstanceIds=[instance_id],
                DocumentName=ssm_document,
                CloudWatchOutputConfig={
                    'CloudWatchLogGroupName': event['ResourceProperties']['LogGroupName'],
                    'CloudWatchOutputEnabled': True
                })

            responseData = {'Success': 'Started bootstrapping for instance: '+instance_id}
        except Exception as e:
            status = cfnresponse.FAILED
            tb_err = traceback.format_exc()
            print(tb_err)
            responseData = {'Error': tb_err}
        finally:
            cfnresponse.send(event, context, status, responseData, 'CustomResourcePhysicalID')
```

```yaml
Lambda_BootstrapTrigger:
  FunctionName: "Auto-generated"
  Runtime: "python3.12"
  Handler: "index.lambda_handler"
  Timeout: 900
  Role: "BootstrapFunctionRole ARN"
  Code: "Inline code above"

CustomResource_BootstrapTrigger:
  ServiceToken: "BootstrapTrigger Lambda ARN"
  Properties:
    InstanceId: "EC2 Instance ID"
    SsmDocument: "Bootstrap SSM Document Name"
    LogGroupName: "IDELogGroup Name"
```

**Note for Terraform:** Use `null_resource` with `local-exec` provisioner or AWS SSM association.

</details>

<details>
<summary><b>MODULE: ide (Lambda - Password Exporter)</b></summary>

## Lambda Function: Export Password from Secrets Manager

```python
# lambda/password-exporter/index.py
import traceback
import cfnresponse
import boto3
import json

def lambda_handler(event, context):
    print('Event: {}'.format(event))
    print('context: {}'.format(context))
    responseData = {}

    status = cfnresponse.SUCCESS

    if event['RequestType'] == 'Delete':
        cfnresponse.send(event, context, status, responseData, 'CustomResourcePhysicalID')
    else:
        try:
            passwordName = event['ResourceProperties']['PasswordName']

            secretsmanager = boto3.client('secretsmanager')

            response = secretsmanager.get_secret_value(
                SecretId=passwordName,
            )
            
            responseData = json.loads(response['SecretString'])
        except Exception as e:
            status = cfnresponse.FAILED
            tb_err = traceback.format_exc()
            print(tb_err)
            responseData = {'Error': tb_err}
        finally:
            cfnresponse.send(event, context, status, responseData, 'CustomResourcePhysicalID')
```

```yaml
Lambda_PasswordExporter:
  FunctionName: "Auto-generated"
  Runtime: "python3.12"
  Handler: "index.lambda_handler"
  Timeout: 180
  Role: "PasswordExporterRole ARN"
  Code: "Inline code above"

CustomResource_PasswordExporter:
  ServiceToken: "PasswordExporter Lambda ARN"
  Properties:
    PasswordName: "First two segments of Secret ARN (secret-id)"
```

**Note for Terraform:** Can use `aws_secretsmanager_secret_version` data source instead.

</details>

<details>
<summary><b>MODULE: ide (SSM Document - Bootstrap)</b></summary>

## SSM Document: Bootstrap IDE Instance

**See next section for the complete bootstrap script - it's very long**

```yaml
SSM_BootstrapDocument:
  Name: "IDE-Bootstrap"
  DocumentType: "Command"
  DocumentFormat: "YAML"
  UpdateMethod: "NewVersion"
  
  Content:
    schemaVersion: "2.2"
    description: "Bootstrap IDE"
    
    parameters:
      BootstrapScript:
        type: "String"
        description: "(Optional) Custom bootstrap script to run."
        default: ""
    
    mainSteps:
      - action: "aws:runShellScript"
        name: "IdeBootstrapFunction"
        inputs:
          runCommand:
            - "See complete script below"

TemplateVariables:
  passwordName: "First two segments of Secret ARN"
  instanceIamRoleName: "SharedRole name"
  instanceIamRoleArn: "SharedRole ARN"
  domain: "CloudFront Distribution Domain Name"
  codeServerVersion: "4.93.1"
  waitConditionHandleUrl: "WaitCondition Handle URL"
  customBootstrapScript: "See custom bootstrap script section"
  installGitea: "See Gitea installation script section"
  splashUrl: ""
  readmeUrl: ""
  environmentContentsZip: ""
  extensions: ""
  terminalOnStartup: "false"
```

</details>

<details>
<summary><b>MODULE: ide (Bootstrap Script - Part 1: Core Setup)</b></summary>

## Complete Bootstrap Script - Part 1

```bash
#!/bin/bash
set -e

echo "Retrieving IDE password..."

PASSWORD_SECRET_VALUE=$(aws secretsmanager get-secret-value --secret-id "${passwordName}" --query 'SecretString' --output text)

export IDE_PASSWORD=$(echo "$PASSWORD_SECRET_VALUE" | jq -r '.password')

echo "Setting profile variables..."

# Set some useful variables
export TOKEN=$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
export AWS_REGION=$(curl -H "X-aws-ec2-metadata-token: $TOKEN" -s http://169.254.169.254/latest/dynamic/instance-identity/document | grep region | awk -F\" '{print $4}')
export EC2_PRIVATE_IP=$(curl -H "X-aws-ec2-metadata-token: $TOKEN" -s http://169.254.169.254/latest/meta-data/local-ipv4)

tee /etc/profile.d/workshop.sh <<EOF
export INSTANCE_IAM_ROLE_NAME="${instanceIamRoleName}"
export INSTANCE_IAM_ROLE_ARN="${instanceIamRoleArn}"

export AWS_REGION="$AWS_REGION"
export EC2_PRIVATE_IP="$EC2_PRIVATE_IP"

export IDE_DOMAIN="${domain}"
export IDE_URL="https://${domain}"
export IDE_PASSWORD="$IDE_PASSWORD"

alias code="code-server"
EOF

source /etc/profile.d/workshop.sh

echo "Setting PS1..."

# Set PS1
tee /etc/profile.d/custom_prompt.sh <<EOF
#!/bin/sh

export PROMPT_COMMAND='export PS1="\u:\w:$ "'
EOF

echo "Generating SSH key..."

# Generate an SSH key for ec2-user
sudo -u ec2-user bash -c "ssh-keygen -t rsa -N '' -f ~/.ssh/id_rsa -m pem <<< y"

echo "Installing AWS CLI..."

# Install AWS CLI
curl -LSsf -o /tmp/aws-cli.zip https://awscli.amazonaws.com/awscli-exe-linux-$(uname -m).zip
unzip -q -d /tmp /tmp/aws-cli.zip
/tmp/aws/install --update
rm -rf /tmp/aws

echo "Installing Docker..."

# Install docker and base package
dnf install -y -q docker git
service docker start
usermod -aG docker ec2-user

echo "Installing code-server..."

# Install code-server
codeServer=$(dnf list installed code-server | wc -l)
if [ "$codeServer" -eq "0" ]; then
  sudo -u ec2-user "codeServerVersion=${codeServerVersion}" bash -c 'curl -fsSL https://code-server.dev/install.sh | sh -s -- --version ${codeServerVersion}'
  systemctl enable --now code-server@ec2-user
fi

sudo -u ec2-user bash -c 'mkdir -p ~/.config/code-server'
sudo -u ec2-user bash -c 'touch ~/.config/code-server/config.yaml'
tee /home/ec2-user/.config/code-server/config.yaml <<EOF
cert: false
auth: password
password: "$IDE_PASSWORD"
bind-addr: 127.0.0.1:8889
EOF

# Create default directory for workspace
sudo -u ec2-user bash -c 'mkdir -p ~/environment'
```

</details>

<details>
<summary><b>MODULE: ide (Bootstrap Script - Part 2: Code-Server Config)</b></summary>

## Bootstrap Script - Part 2: Code-Server Configuration

```bash
ENVIRONMENT_CONTENTS_ZIP=${environmentContentsZip}

if [ ! -z "$ENVIRONMENT_CONTENTS_ZIP" ]; then
  echo "Adding environments archive..."

  if [[ $ENVIRONMENT_CONTENTS_ZIP == s3:* ]]; then
    aws s3 cp $ENVIRONMENT_CONTENTS_ZIP /tmp/environment.zip
  else
    curl -LSsf -o /tmp/environment.zip $ENVIRONMENT_CONTENTS_ZIP
  fi

  sudo -u ec2-user bash -c 'unzip -q /tmp/environment.zip -d ~/environment'

  rm -rf /tmp/environment.zip
fi

STARTUP_EDITOR='none'

TERMINAL_ON_STARTUP="${terminalOnStartup}"
README_URL="${readmeUrl}"

if [ ! -z "$README_URL" ]; then
  echo "Adding README..."
  if [[ $README_URL == s3:* ]]; then
    aws s3 cp $README_URL /home/ec2-user/environment/README.md
  else
    curl -LSsf -o /home/ec2-user/environment/README.md $README_URL
  fi
fi

if [ "$TERMINAL_ON_STARTUP" = "true" ]; then
  STARTUP_EDITOR='terminal'
elif [ -f /home/ec2-user/environment/README.md ]; then
  STARTUP_EDITOR='readme'
fi

echo "Configuring code-server..."

sudo -u ec2-user bash -c 'mkdir -p ~/.local/share/code-server/User'
sudo -u ec2-user bash -c 'touch ~/.local/share/code-server/User/settings.json'
tee /home/ec2-user/.local/share/code-server/User/settings.json <<EOF
{
  "extensions.autoUpdate": false,
  "extensions.autoCheckUpdates": false,
  "security.workspace.trust.enabled": false,
  "workbench.startupEditor": "$STARTUP_EDITOR",
  "task.allowAutomaticTasks": "on",
  "telemetry.telemetryLevel": "off"
}
EOF

sudo -u ec2-user bash -c 'touch ~/.local/share/code-server/User/keybindings.json'
tee /home/ec2-user/.local/share/code-server/User/keybindings.json << 'EOF'
[
  {
    "key": "shift+cmd+/",
    "command": "remote.tunnel.forwardCommandPalette"
  }
]
EOF

if [ ! -z "${splashUrl}" ]; then
echo "Configuring splash URL..."

sudo -u ec2-user bash -c 'touch ~/.local/share/code-server/User/tasks.json'
tee /home/ec2-user/.local/share/code-server/User/tasks.json << 'EOF'
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Open Splash",
      "command": "${!input:openSimpleBrowser}",
      "presentation": {
        "reveal": "always",
        "panel": "new"
      },
      "runOptions": {
        "runOn": "folderOpen"
      }
    }
  ],
  "inputs": [
    {
      "id": "openSimpleBrowser",
      "type": "command",
      "command": "simpleBrowser.show",
      "args": [
        "${splashUrl}"
      ]
    }
  ]
}
EOF
fi

echo "Installing code-server extensions..."

EXTENSIONS="${extensions}"

IFS=',' read -ra array <<< "$EXTENSIONS"

# Iterate over each entry in the array
for extension in "${!array[@]}"; do
  # Use retries as extension installation seems unreliable
  sudo -u ec2-user bash -c "set -e; (r=5;while ! code-server --install-extension $extension --force ; do ((--r))||exit;sleep 5;done)"
done

if [ ! -f "/home/ec2-user/.local/share/code-server/coder.json" ]; then
  sudo -u ec2-user bash -c 'touch ~/.local/share/code-server/coder.json'
  echo '{ "query": { "folder": "/home/ec2-user/environment" } }' > /home/ec2-user/.local/share/code-server/coder.json
fi

echo "Restarting code-server..."

systemctl restart code-server@ec2-user

echo "Installing Caddy..."

# Install caddy
dnf copr enable -y -q @caddy/caddy epel-9-x86_64
dnf install -y -q caddy
systemctl enable --now caddy

tee /etc/caddy/Caddyfile <<EOF
http://${domain} {
  handle /* {
    reverse_proxy 127.0.0.1:8889
  }
  #GITEA
}
EOF

echo "Restarting caddy..."

systemctl restart caddy

if [ ! -f "/home/ec2-user/.local/share/code-server/coder.json" ]; then
  sudo -u ec2-user bash -c 'touch ~/.local/share/code-server/coder.json'
  echo '{ "query": { "folder": "/home/ec2-user/environment" } }' > /home/ec2-user/.local/share/code-server/coder.json
fi
```

</details>

<details>
<summary><b>MODULE: ide (Bootstrap Script - Part 3: Gitea Installation)</b></summary>

## Gitea Installation Script

```bash
dnf install -y nerdctl cni-plugins
mkdir -p /gitea/config /gitea/data

echo '
version: "2"

services:
  gitea:
    image: gitea/gitea:1.22-rootless
    restart: always
    volumes:
      - /gitea/data:/var/lib/gitea
      - /gitea/config:/etc/gitea
      - /etc/timezone:/etc/timezone:ro
      - /etc/localtime:/etc/localtime:ro
    ports:
      - "9999:3000"
      - "2222:2222"
' > gitea.yaml

echo "
APP_NAME = Gitea: Git with a cup of tea
RUN_MODE = prod
RUN_USER = git
WORK_PATH = /var/lib/gitea

[repository]
ROOT = /var/lib/gitea/git/repositories
ENABLE_PUSH_CREATE_USER = true
DISABLE_HTTP_GIT = false

[repository.local]
LOCAL_COPY_PATH = /var/lib/gitea/tmp/local-repo

[repository.upload]
TEMP_PATH = /var/lib/gitea/uploads

[server]
APP_DATA_PATH = /var/lib/gitea
DOMAIN = $EC2_PRIVATE_IP
SSH_DOMAIN = $EC2_PRIVATE_IP
SSH_CREATE_AUTHORIZED_KEYS_FILE=false
HTTP_PORT = 3000
ROOT_URL = http://$EC2_PRIVATE_IP:9000/gitea
DISABLE_SSH = false
SSH_PORT = 2222
SSH_LISTEN_PORT = 2222
START_SSH_SERVER = true
LFS_START_SERVER = true
OFFLINE_MODE = true

[database]
PATH = /var/lib/gitea/gitea.db
DB_TYPE = sqlite3
HOST = localhost:3306
NAME = gitea
USER = root
PASSWD = 
LOG_SQL = false
SCHEMA = 
SSL_MODE = disable

[indexer]
ISSUE_INDEXER_PATH = /var/lib/gitea/indexers/issues.bleve

[session]
PROVIDER_CONFIG = /var/lib/gitea/sessions
PROVIDER = file

[picture]
AVATAR_UPLOAD_PATH = /var/lib/gitea/avatars
REPOSITORY_AVATAR_UPLOAD_PATH = /var/lib/gitea/repo-avatars

[attachment]
PATH = /var/lib/gitea/attachments

[log]
MODE = console
LEVEL = info
ROOT_PATH = /var/lib/gitea/log

[security]
INSTALL_LOCK = true
SECRET_KEY = 
REVERSE_PROXY_LIMIT = 1
REVERSE_PROXY_TRUSTED_PROXIES = *
PASSWORD_HASH_ALGO = pbkdf2

[service]
DISABLE_REGISTRATION = true
REQUIRE_SIGNIN_VIEW = true
REGISTER_EMAIL_CONFIRM = false
ENABLE_NOTIFY_MAIL = false
ALLOW_ONLY_EXTERNAL_REGISTRATION = false
ENABLE_CAPTCHA = false
DEFAULT_KEEP_EMAIL_PRIVATE = false
DEFAULT_ALLOW_CREATE_ORGANIZATION = true
DEFAULT_ENABLE_TIMETRACKING = true
NO_REPLY_ADDRESS = noreply.localhost

[lfs]
PATH = /var/lib/gitea/git/lfs

[mailer]
ENABLED = false

[cron.update_checker]
ENABLED = false

[repository.pull-request]
DEFAULT_MERGE_STYLE = merge

[repository.signing]
DEFAULT_TRUST_MODEL = committer

" > /gitea/config/app.ini
chown -R 1000:1000 /gitea
sudo nerdctl compose -f gitea.yaml up -d --quiet-pull

# We need to be idempotent and check for locked database
while true; do
    CONTAINER=$(sudo nerdctl compose -f gitea.yaml ps --format json | jq .[].Name)

    if [ ! -z "$CONTAINER" ]; then
      STATUS=$(sudo nerdctl exec $CONTAINER -- sh -c "gitea admin user create --username workshop-user --email workshop-user@example.com --password $IDE_PASSWORD 2>&1 || exit 0")
      [[ "$STATUS" =~ .*locked|no\ such\ table.* ]] || break
    fi
    sleep 5;
done

tee -a /etc/caddy/Caddyfile <<EOF
http://$IDE_DOMAIN:9000, http://localhost:9000 {
  handle_path /proxy/9000/* {
    reverse_proxy 127.0.0.1:9999
  }

  handle /* {
    reverse_proxy 127.0.0.1:9999
  }
}
EOF

# We add the handle_path in the cloudfront site
sed -i 's~#GITEA~handle_path /gitea/* { \
    reverse_proxy 127.0.0.1:9999 \
  }~' /etc/caddy/Caddyfile

systemctl restart caddy

sleep 5

sudo -u ec2-user bash -c 'git config --global user.email "workshop-user@example.com"'
sudo -u ec2-user bash -c 'git config --global user.name "Workshop User"'

sudo -u ec2-user bash -c 'touch ~/.ssh/config'
tee /home/ec2-user/.ssh/config <<EOF
Host $EC2_PRIVATE_IP
  User git
  Port 2222
  IdentityFile /home/ec2-user/.ssh/id_rsa
  IdentitiesOnly yes
EOF

sudo -u ec2-user bash -c 'chmod 600 ~/.ssh/*'

PUB_KEY=$(sudo cat /home/ec2-user/.ssh/id_rsa.pub)
TITLE="$(hostname)$(date +%s)"

curl -X 'POST' \
  "http://workshop-user:$IDE_PASSWORD@localhost:9000/api/v1/user/keys" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d "{
  \"key\": \"$PUB_KEY\",
  \"read_only\": true,
  \"title\": \"$TITLE\"
}"

tee /etc/profile.d/gitea.sh <<EOF
export GIT_SSH_ENDPOINT="$EC2_PRIVATE_IP:2222"
export GITEA_API_ENDPOINT="http://$EC2_PRIVATE_IP:9000"
export GITEA_EXTERNAL_URL="https://$IDE_DOMAIN/gitea"
export GITEA_PASSWORD="$IDE_PASSWORD"
EOF

source /etc/profile.d/gitea.sh
```

**Gitea Configuration Values:**
```yaml
Gitea:
  Image: "gitea/gitea:1.22-rootless"
  ContainerHttpPort: 3000
  HostHttpPort: 9999
  ContainerSshPort: 2222
  HostSshPort: 2222
  Username: "workshop-user"
  Email: "workshop-user@example.com"
  Database: "sqlite3"
  DatabasePath: "/var/lib/gitea/gitea.db"
```

</details>

<details>
<summary><b>MODULE: ide (Bootstrap Script - Part 4: Custom Workshop Setup)</b></summary>

## Custom Bootstrap Script for Workshop

```bash
#!/bin/bash
set -x
sudo sh -c "echo LANG=en_US.utf-8 >> /etc/environment"
sudo sh -c "echo LC_ALL=en_US.UTF-8 >> /etc/environment"

sudo yum -y install sqlite telnet jq strace tree gcc glibc-static python3 python3-pip gettext bash-completion npm zsh util-linux-user locate

echo '=== INSTALL and CONFIGURE default software components ==='

aws configure set cli_pager ""

export TOKEN=$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
export AWS_REGION=$(curl -H "X-aws-ec2-metadata-token: $TOKEN" -s http://169.254.169.254/latest/dynamic/instance-identity/document | grep region | awk -F\" '{print $4}')
export ACCOUNTID=$(aws sts get-caller-identity | jq -r .Account)
export AWS_ACCOUNT_ID=$ACCOUNTID
export ACCOUNT_ID=$ACCOUNTID
export ASSETS_BUCKET_NAME=${AssetsBucketName}
export ASSETS_BUCKET_PREFIX=${AssetsBucketPrefix}
export BUCKET_NAME=${BUCKET_NAME}
export WORKSHOP_GIT_URL=${WORKSHOP_GIT_URL}
export WORKSHOP_GIT_BRANCH=${WORKSHOP_GIT_BRANCH}
export BASE_DIR=/home/ec2-user/environment/fleet-management-on-amazon-eks-workshop
export GITOPS_DIR=/home/ec2-user/environment/gitops-repos
export GOROOT=/usr/local/go

# This is to go around problem with circular dependency
aws ssm put-parameter --type String --name GiteaExternalUrl --value $GITEA_EXTERNAL_URL --overwrite

# Create wait-for-lb script
sudo bash -c "cat > /usr/local/bin/wait-for-lb" <<'EOT'
#!/bin/bash
set -e
export host=$1

if [ -z "$host" ]; then
echo "the service is not found: $host"
exit
fi

echo $host

set -Eeuo pipefail

echo "Waiting for $host..."

EXIT_CODE=0

timeout -s TERM 600 bash -c \
'while [[ "$(curl -s -o /dev/null -L -w ''%{http_code}'' http://$host/home)" != "200" ]];\
do sleep 5;\
done' || EXIT_CODE=$?

if [ $EXIT_CODE -ne 0 ]; then
echo "Load balancer did not become available or return HTTP 200 for 600 seconds"
exit 1
fi

echo "You can now access http://$host"
EOT
sudo chmod 755 /usr/local/bin/wait-for-lb

# Create wait-for-lb-argocd script
sudo bash -c "cat > /usr/local/bin/wait-for-lb-argocd" <<'EOT'
#!/bin/bash
set -e
export host=$1

if [ -z "$host" ]; then
echo "the service is not found: $host"
exit
fi

echo $host

set -Eeuo pipefail

echo "Waiting for $host..."

EXIT_CODE=0

timeout -s TERM 600 bash -c \
'while [[ "$(curl -s -k -o /dev/null -L -w ''%{http_code}'' http://$host/)" != "200" ]];\
do sleep 5;\
done' || EXIT_CODE=$?

if [ $EXIT_CODE -ne 0 ]; then
echo "Load balancer did not become available or return HTTP 200 for 600 seconds"
exit 1
fi

echo "You can now access http://$host"
EOT
sudo chmod 755 /usr/local/bin/wait-for-lb-argocd

# Install kubectl
sudo curl --silent --location -o /usr/bin/kubectl https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
sudo chmod +x /usr/bin/kubectl

# Install ArgoCD CLI
sudo curl --silent --location -o /usr/bin/argocd https://github.com/argoproj/argo-cd/releases/download/v2.12.2/argocd-linux-amd64
sudo chmod +x /usr/bin/argocd

# Install Helm
curl --silent --location "https://get.helm.sh/helm-v3.10.1-linux-amd64.tar.gz" | tar xz -C /tmp
sudo mv -f /tmp/linux-amd64/helm /usr/bin
sudo chmod +x /usr/bin/helm

# Install Terraform
sudo curl --silent --location -o /tmp/terraform.zip "https://releases.hashicorp.com/terraform/1.9.3/terraform_1.9.3_linux_amd64.zip"
cd /tmp && unzip -o /tmp/terraform.zip && cd -
chmod +x /tmp/terraform
sudo mv -f /tmp/terraform /usr/bin

# Install Go
sudo curl --silent --location "https://go.dev/dl/go1.23.1.linux-amd64.tar.gz" | sudo tar xz -C /usr/local

# User-specific setup
sudo su - ec2-user <<EOF
set -x
export | sort

aws configure set cli_pager ""

# Shell completions
kubectl completion bash >>  ~/.bash_completion
argocd completion bash >>  ~/.bash_completion
helm completion bash >>  ~/.bash_completion

# Aliases
echo "alias k=kubectl" >> ~/.bashrc
echo "alias kgn='kubectl get nodes -L beta.kubernetes.io/arch -L eks.amazonaws.com/capacityType -L beta.kubernetes.io/instance-type -L eks.amazonaws.com/nodegroup -L topology.kubernetes.io/zone -L karpenter.sh/provisioner-name -L karpenter.sh/capacity-type'" | tee -a ~/.bashrc
echo "alias ll='ls -la'" >> ~/.bashrc
echo "alias ktx=kubectx" >> ~/.bashrc
echo "alias kctx=kubectx" >> ~/.bashrc
echo "alias kns=kubens" >> ~/.bashrc
echo "export TERM=xterm-color" >> ~/.bashrc
echo "alias code=/usr/lib/code-server/bin/code-server" >> ~/.bashrc
echo "complete -F __start_kubectl k" >> ~/.bashrc

# Install k9s
curl -sS https://webinstall.dev/k9s | bash

# Install Krew and kubectl plugins
(
  cd \$(mktemp -d) && pwd &&
  OS=\$(uname | tr '[:upper:]' '[:lower:]') &&
  ARCH=\$(uname -m | sed -e 's/x86_64/amd64/' -e 's/\(arm\)\(64\)\?.*/\1\2/' -e 's/aarch64$/arm64/') &&
  KREW=krew-\${!OS}_\${!ARCH} && echo \$KREW
  curl -fsSLO https://github.com/kubernetes-sigs/krew/releases/latest/download/\${!KREW}.tar.gz &&
  tar zxvf \${!KREW}.tar.gz &&
  ./\${!KREW} install krew
)

echo "export PATH=\${!KREW_ROOT:-/home/ec2-user/.krew}/bin:/home/ec2-user/.local/bin:/usr/local/go/bin:~/go/bin:\$PATH" | tee -a ~/.bashrc
export PATH=\${!KREW_ROOT:-/home/ec2-user/.krew}/bin:/home/ec2-user/.local/bin:/usr/local/go/bin:~/go/bin:\$PATH

kubectl krew install stern
kubectl krew install np-viewer 

# Install Go tools
go install github.com/kyverno/chainsaw@latest

# Install Python packages
pip install pytest pytest_bdd boto3 kubernetes

# Install direnv
curl -sfL https://direnv.net/install.sh | bash

# Install VS Code extensions
/usr/lib/code-server/bin/code-server --install-extension hashicorp.terraform || true
/usr/lib/code-server/bin/code-server --install-extension moshfeu.compare-folders || true
/usr/lib/code-server/bin/code-server --install-extension amazonwebservices.amazon-q-vscode || true

# Install Amazon Q
curl --proto '=https' --tlsv1.2 -sSf "https://desktop-release.q.us-east-1.amazonaws.com/latest/q-x86_64-linux.zip" -o "/tmp/q.zip"
unzip /tmp/q.zip -d /tmp
/tmp/q/install.sh --no-confirm

# Install ag (silver searcher)
sudo dnf install -y git gcc make pkg-config automake autoconf pcre-devel xz-devel zlib-devel
cd /tmp && git clone https://github.com/ggreer/the_silver_searcher.git && \
cd the_silver_searcher && \
./build.sh && \
sudo make install

# Install fzf
git clone --depth 1 https://github.com/junegunn/fzf.git ~/.fzf
~/.fzf/install --all  

# Install and configure zsh
sudo -k chsh -s /bin/zsh ec2-user
jq '. + {"terminal.integrated.defaultProfile.linux": "zsh"}' /home/ec2-user/.local/share/code-server/User/settings.json > temp.json && mv temp.json /home/ec2-user/.local/share/code-server/User/settings.json
rm -rf ~/.oh-my-zsh

wget https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh
CHSH=no RUNZSH=no sh install.sh

git clone https://github.com/romkatv/powerlevel10k.git ~/.oh-my-zsh/custom/themes/powerlevel10k
git clone https://github.com/zsh-users/zsh-syntax-highlighting.git ~/.oh-my-zsh/custom/plugins/zsh-syntax-highlighting
git clone https://github.com/zsh-users/zsh-autosuggestions ~/.oh-my-zsh/custom/plugins/zsh-autosuggestions
git clone https://github.com/zsh-users/zsh-history-substring-search ~/.oh-my-zsh/custom/plugins/zsh-history-substring-search

# Clone workshop repository
mkdir -p \$BASE_DIR
git clone \$WORKSHOP_GIT_URL \$BASE_DIR
cd \$BASE_DIR
git checkout \$WORKSHOP_GIT_BRANCH

cp hack/.zshrc hack/.p10k.zsh ~/

# Setup bashrc.d
mkdir -p ~/.bashrc.d
cp \$BASE_DIR/hack/.bashrc.d/* ~/.bashrc.d/

# Create Terraform backend configs
cat << EOT > \$BASE_DIR/terraform/common/backend_override.tf
terraform {
  backend "s3" {
    bucket         = "\$BUCKET_NAME"
    key            = "common/terraform.tfstate"
    region         = "\$AWS_REGION"
  }
}
EOT

cat << EOT > \$BASE_DIR/terraform/hub/backend_override.tf
terraform {
  backend "s3" {
    bucket         = "\$BUCKET_NAME"
    key            = "hub/terraform.tfstate"
    region         = "\$AWS_REGION"
  }
}
EOT

cat << EOT > \$BASE_DIR/terraform/spokes/backend_override.tf
terraform {
  backend "s3" {
    bucket         = "\$BUCKET_NAME"
    key            = "spokes/terraform.tfstate"
    region         = "\$AWS_REGION"
    workspace_key_prefix = "spokes"
  }
}
EOT

EOF

# Install kubectx & kubens
sudo rm -rf /opt/kubectx
sudo git clone https://github.com/ahmetb/kubectx /opt/kubectx
sudo ln -sf /opt/kubectx/kubectx /usr/local/bin/kubectx
sudo ln -sf /opt/kubectx/kubens /usr/local/bin/kubens

# Install eks-node-viewer
sudo curl -L https://github.com/awslabs/eks-node-viewer/releases/download/v0.7.1/eks-node-viewer_Linux_x86_64 -o /usr/local/bin/eks-node-viewer && sudo chmod +x $_

source ~/.bashrc

# Configure .bashrc.d
if [[ ! -d "/home/ec2-user/.bashrc.d" ]]; then
    sudo -H -u ec2-user bash -c "mkdir -p ~/.bashrc.d"
    cat << EOT > /home/ec2-user/.bashrc.d/env.bash
    export ACCOUNTID=$ACCOUNTID
    export ACCOUNT_ID=$ACCOUNTID
    export AWS_ACCOUNT_ID=$ACCOUNTID
    export AWS_DEFAULT_REGION=$AWS_REGION
    export GOROOT=/usr/local/go
EOT

    sudo -H -u ec2-user bash -c "cat <<'EOF' >> ~/.bashrc 
for file in ~/.bashrc.d/*.bash; do
  source \"\$file\" || true
done
EOF
"
fi

echo '=== CONFIGURE awscli and setting ENVIRONMENT VARS ==='
echo "complete -C '/usr/local/bin/aws_completer' aws" >> /home/ec2-user/.bashrc.d/aws.bash

echo '=== CLEANING /home/ec2-user ==='
chown -R ec2-user:ec2-user /home/ec2-user/

echo "Bootstrap completed with return code $?"
```

**Variables used in this script:**
```yaml
AssetsBucketName: "From Mappings based on region"
AssetsBucketPrefix: "28c283c1-1d60-43fa-a604-4e983e0e8038/"
BUCKET_NAME: "TFStateBackendBucket name"
WORKSHOP_GIT_URL: "https://github.com/aws-samples/fleet-management-on-amazon-eks-workshop"
WORKSHOP_GIT_BRANCH: "v1.0.2"
```

**Tool Versions:**
```yaml
ToolVersions:
  Kubectl: "Latest stable"
  ArgoCD: "v2.12.2"
  Helm: "v3.10.1"
  Terraform: "1.9.3"
  Go: "1.23.1"
  EksNodeViewer: "v0.7.1"
```

</details>

<details>
<summary><b>MODULE: codebuild (Lambda - Start Build)</b></summary>

## Lambda Function: Start CodeBuild Project

```javascript
// lambda/start-build/index.js
const respond = async function(event, context, responseStatus, responseData, physicalResourceId, noEcho) {
  return new Promise((resolve, reject) => {
    var responseBody = JSON.stringify({
        Status: responseStatus,
        Reason: "See the details in CloudWatch Log Stream: " + context.logGroupName + " " + context.logStreamName,
        PhysicalResourceId: physicalResourceId || context.logStreamName,
        StackId: event.StackId,
        RequestId: event.RequestId,
        LogicalResourceId: event.LogicalResourceId,
        NoEcho: noEcho || false,
        Data: responseData
    });

    console.log("Response body:\n", responseBody);

    var https = require("https");
    var url = require("url");

    var parsedUrl = url.parse(event.ResponseURL);
    var options = {
        hostname: parsedUrl.hostname,
        port: 443,
        path: parsedUrl.path,
        method: "PUT",
        headers: {
            "content-type": "",
            "content-length": responseBody.length
        }
    };

    var request = https.request(options, function(response) {
        console.log("Status code: " + response.statusCode);
        console.log("Status message: " + response.statusMessage);
        resolve();
    });

    request.on("error", function(error) {
        console.log("respond(..) failed executing https.request(..): " + error);
        resolve();
    });

    request.write(responseBody);
    request.end();
  });
};

const AWS = require('aws-sdk');

exports.handler = async function (event, context) {
  console.log(JSON.stringify(event, null, 4));
  try {
    const projectName = event.ResourceProperties.ProjectName;
    const codeBuildIamRoleArn = event.ResourceProperties.CodeBuildIamRoleArn

    const codebuild = new AWS.CodeBuild();

    console.log(`Starting new build of project ${projectName}`);

    const { build } = await codebuild.startBuild({
      projectName,
      environmentVariablesOverride: [
        {
          name: 'CFN_RESPONSE_URL',
          value: event.ResponseURL
        },
        {
          name: 'CFN_STACK_ID',
          value: event.StackId
        },
        {
          name: 'CFN_REQUEST_ID',
          value: event.RequestId
        },
        {
          name: 'CFN_LOGICAL_RESOURCE_ID',
          value: event.LogicalResourceId
        },
        {
          name: 'REQUESTED_ACTION',
          value: event.RequestType
        },
        {
          name: 'RESOURCE_CODEBUILD_ROLE_ARN',
          value: codeBuildIamRoleArn
        }
      ]
    }).promise();
    console.log(`Build id ${build.id} started - resource completion handled by EventBridge`);
  } catch(error) {
    console.error(error);
    await respond(event, context, 'FAILED', { Error: error });
  }
};
```

```yaml
Lambda_StartBuild:
  FunctionName: "Auto-generated"
  Runtime: "nodejs16.x"
  Handler: "index.handler"
  Timeout: 60
  Role: "StartBuildRole ARN"
  Code: "Inline code above"
```

</details>

<details>
<summary><b>MODULE: codebuild (Lambda - Report Build)</b></summary>

## Lambda Function: Report CodeBuild Status

```javascript
// lambda/report-build/index.js
const respond = async function(event, context, responseStatus, responseData, physicalResourceId, noEcho) {
  return new Promise((resolve, reject) => {
    var responseBody = JSON.stringify({
        Status: responseStatus,
        Reason: "See the details in CloudWatch Log Stream: " + context.logGroupName + " " + context.logStreamName,
        PhysicalResourceId: physicalResourceId || context.logStreamName,
        StackId: event.StackId,
        RequestId: event.RequestId,
        LogicalResourceId: event.LogicalResourceId,
        NoEcho: noEcho || false,
        Data: responseData
    });

    console.log("Response body:\n", responseBody);

    var https = require("https");
    var url = require("url");

    var parsedUrl = url.parse(event.ResponseURL);
    var options = {
        hostname: parsedUrl.hostname,
        port: 443,
        path: parsedUrl.path,
        method: "PUT",
        headers: {
            "content-type": "",
            "content-length": responseBody.length
        }
    };

    var request = https.request(options, function(response) {
        console.log("Status code: " + response.statusCode);
        console.log("Status message: " + response.statusMessage);
        resolve();
    });

    request.on("error", function(error) {
        console.log("respond(..) failed executing https.request(..): " + error);
        resolve();
    });

    request.write(responseBody);
    request.end();
  });
};

const AWS = require('aws-sdk');

exports.handler = async function (event, context) {
  console.log(JSON.stringify(event, null, 4));

  const projectName = event['detail']['project-name'];

  const codebuild = new AWS.CodeBuild();

  const buildId = event['detail']['build-id'];
  const { builds } = await codebuild.batchGetBuilds({
    ids: [ buildId ]
  }).promise();

  console.log(JSON.stringify(builds, null, 4));

  const build = builds[0];
  const environment = {};
  build.environment.environmentVariables.forEach(e => environment[e.name] = e.value);

  const response = {
    ResponseURL: environment.CFN_RESPONSE_URL,
    StackId: environment.CFN_STACK_ID,
    LogicalResourceId: environment.CFN_LOGICAL_RESOURCE_ID,
    RequestId: environment.CFN_REQUEST_ID
  };

  if (event['detail']['build-status'] === 'SUCCEEDED') {
    await respond(response, context, 'SUCCESS', {}, 'build');
  } else {
    await respond(response, context, 'FAILED', { Error: 'Build failed' });
  }
};
```

```yaml
Lambda_ReportBuild:
  FunctionName: "Auto-generated"
  Runtime: "nodejs16.x"
  Handler: "index.handler"
  Timeout: 60
  Role: "ReportBuildRole ARN"
  Code: "Inline code above"
```

</details>

<details>
<summary><b>MODULE: codebuild (CodeBuild Projects - Common Config)</b></summary>

## CodeBuild Common Configuration

```yaml
CodeBuildCommon:
  ServiceRole: "SharedRole ARN"
  
  Artifacts:
    Type: "NO_ARTIFACTS"
  
  Cache:
    Type: "NO_CACHE"
  
  EncryptionKey: "alias/aws/s3"
  
  Environment:
    ComputeType: "BUILD_GENERAL1_SMALL"
    Image: "aws/codebuild/amazonlinux2-x86_64-standard:4.0"
    ImagePullCredentialsType: "CODEBUILD"
    PrivilegedMode: false
    Type: "LINUX_CONTAINER"
  
  TimeoutInMinutes: 60
  
  Source:
    Type: "NO_SOURCE"
    BuildSpec: "Inline BuildSpec (see below)"
```

</details>

<details>
<summary><b>MODULE: codebuild (Project 1: GitOps/Common)</b></summary>

## CodeBuild Project: GitOps/Common IAM Stack

```yaml
Project_GitOps:
  Name: "EKS-GITOPS-IAM-Stack-Deploy"
  Description: "Deploy GitOps and IAM configuration"
  
  EnvironmentVariables:
    - Name: "TFSTATE_BUCKET_NAME"
      Type: "PLAINTEXT"
      Value: "TFStateBackendBucket Name"
    
    - Name: "WORKSHOP_GIT_URL"
      Type: "PLAINTEXT"
      Value: "https://github.com/aws-samples/fleet-management-on-amazon-eks-workshop"
    
    - Name: "WORKSHOP_GIT_BRANCH"
      Type: "PLAINTEXT"
      Value: "v1.0.2"
    
    - Name: "FORCE_DELETE_VPC"
      Type: "PLAINTEXT"
      Value: "true"
    
    - Name: "GITEA_PASSWORD"
      Type: "PLAINTEXT"
      Value: "IDE Password from Secrets Manager"
    
    - Name: "IS_WS"
      Type: "PLAINTEXT"
      Value: "false"
  
  BuildSpec: |
    version: 0.2
    phases:
      pre_build:
        commands:
          - yum install -y gettext
          - curl --silent --location "https://get.helm.sh/helm-v3.9.2-linux-amd64.tar.gz" | tar xz -C /tmp
          - mv /tmp/linux-amd64/helm /usr/local/bin
          - chmod +x /usr/local/bin/helm
          - sudo yum install -y yum-utils
          - sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo
          - sudo yum -y install terraform
      
      build:
        commands:
          - set -x
          - set -e
          - aws configure set cli_pager ""
          - ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
          - BUCKET_NAME=${TFSTATE_BUCKET_NAME}
          - BASE_DIR=${CODEBUILD_SRC_DIR}
          - WORKSHOP_GIT_URL=${WORKSHOP_GIT_URL:-https://github.com/aws-samples/fleet-management-on-amazon-eks-workshop}
          - WORKSHOP_GIT_BRANCH=${WORKSHOP_GIT_BRANCH:-v1.0.2}
          - IS_WS=${IS_WS:-false}
          - GITEA_PASSWORD=${GITEA_PASSWORD}
          - env | sort
          - git clone $WORKSHOP_GIT_URL $BASE_DIR || true
          - cd $BASE_DIR
          - git checkout $WORKSHOP_GIT_BRANCH
          - cd -
          - aws ssm put-parameter --name "tf-backend-bucket" --type "String" --value "$BUCKET_NAME" --overwrite
          - |
            cat << EOT > $BASE_DIR/terraform/common/backend_override.tf
            terraform {
              backend "s3" {
                bucket         = "$BUCKET_NAME"
                key            = "common/terraform.tfstate"
                region         = "$AWS_REGION"
              }
            }
            EOT
          - mkdir -p ~/.ssh
          - |
            if [[ $REQUESTED_ACTION == 'Delete' ]]; then
              DEBUG=1 TF_VAR_gitea_external_url=$GITEA_EXTERNAL_URL TF_VAR_gitea_password=$GITEA_PASSWORD $BASE_DIR/terraform/common/destroy.sh
            else
              if [[ $IS_WS == "true" ]]; then
                aws securityhub enable-security-hub || true
                QUICKSIGHT_ACCOUNT_NAME="eks-fleet-workshop"
                QS_NOTIFICATION_EMAIL="eks-fleet-workshop@amazon.com"
                FULL_QS_ACCOUNT_NAME="${QUICKSIGHT_ACCOUNT_NAME}-${ACCOUNT_ID}"
                aws quicksight create-account-subscription \
                  --aws-account-id "$ACCOUNT_ID" \
                  --edition ENTERPRISE \
                  --authentication-method 'IAM_AND_QUICKSIGHT' \
                  --account-name "$FULL_QS_ACCOUNT_NAME" \
                  --notification-email "$QS_NOTIFICATION_EMAIL" || true
              fi
              GITEA_EXTERNAL_URL=$(aws ssm get-parameter --name "GiteaExternalUrl" --query "Parameter.Value" --output text || true)
              for i in $(seq 1 60); do
                if [[ -z $GITEA_EXTERNAL_URL ]]; then
                  sleep 10
                  GITEA_EXTERNAL_URL=$(aws ssm get-parameter --name "GiteaExternalUrl" --query "Parameter.Value" --output text || true)
                  echo $GITEA_EXTERNAL_URL
                else
                  break
                fi
              done
              DEBUG=1 TF_VAR_gitea_external_url=$GITEA_EXTERNAL_URL TF_VAR_gitea_password=$GITEA_PASSWORD $BASE_DIR/terraform/common/deploy.sh
            fi
      
      post_build:
        commands:
          - echo ">>> build status $CODEBUILD_BUILD_SUCCEEDING "
```

</details>

<details>
<summary><b>MODULE: codebuild (Project 2: Hub Cluster)</b></summary>

## CodeBuild Project: Hub Cluster

```yaml
Project_Hub:
  Name: "EKS-Hub-Stack-Deploy"
  Description: "Deploy EKS Hub cluster"
  
  EnvironmentVariables:
    - Name: "TFSTATE_BUCKET_NAME"
      Type: "PLAINTEXT"
      Value: "TFStateBackendBucket Name"
    
    - Name: "WORKSHOP_GIT_URL"
      Type: "PLAINTEXT"
      Value: "https://github.com/aws-samples/fleet-management-on-amazon-eks-workshop"
    
    - Name: "WORKSHOP_GIT_BRANCH"
      Type: "PLAINTEXT"
      Value: "v1.0.2"
    
    - Name: "FORCE_DELETE_VPC"
      Type: "PLAINTEXT"
      Value: "true"
  
  BuildSpec: |
    version: 0.2
    phases:
      pre_build:
        commands:
          - yum install -y gettext
          - curl --silent --location "https://get.helm.sh/helm-v3.9.2-linux-amd64.tar.gz" | tar xz -C /tmp
          - mv /tmp/linux-amd64/helm /usr/local/bin
          - chmod +x /usr/local/bin/helm
          - sudo yum install -y yum-utils
          - sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo
          - sudo yum -y install terraform
      
      build:
        commands:
          - set -x
          - set -e
          - aws configure set cli_pager ""
          - ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
          - BUCKET_NAME=${TFSTATE_BUCKET_NAME}
          - BASE_DIR=${CODEBUILD_SRC_DIR}
          - WORKSHOP_GIT_URL=${WORKSHOP_GIT_URL:-https://github.com/aws-samples/fleet-management-on-amazon-eks-workshop}
          - WORKSHOP_GIT_BRANCH=${WORKSHOP_GIT_BRANCH:-v1.0.2}
          - env | sort
          - git clone $WORKSHOP_GIT_URL $BASE_DIR || true
          - cd $BASE_DIR
          - git checkout $WORKSHOP_GIT_BRANCH
          - cd -
          - aws ssm put-parameter --name "tf-backend-bucket" --type "String" --value "$BUCKET_NAME" --overwrite
          - |
            cat << EOT > $BASE_DIR/terraform/hub/backend_override.tf
            terraform {
              backend "s3" {
                bucket         = "$BUCKET_NAME"
                key            = "hub/terraform.tfstate"
                region         = "$AWS_REGION"
              }
            }
            EOT
          - mkdir -p ~/.ssh
          - |
            if [[ $REQUESTED_ACTION == 'Delete' ]]; then
              DEBUG=1 $BASE_DIR/terraform/hub/destroy.sh
            else
              DEBUG=1 $BASE_DIR/terraform/hub/deploy.sh
            fi
      
      post_build:
        commands:
          - echo ">>> build status $CODEBUILD_BUILD_SUCCEEDING "
```

</details>

<details>
<summary><b>MODULE: codebuild (Projects 3 & 4: Spoke Clusters)</b></summary>

## CodeBuild Projects: Spoke Clusters (Staging & Prod)

```yaml
Project_Spoke_Staging:
  Name: "EKS-Spoke-Staging-Stack-Deploy"
  Description: "Deploy EKS Staging spoke cluster"
  
  EnvironmentVariables:
    - Name: "TFSTATE_BUCKET_NAME"
      Type: "PLAINTEXT"
      Value: "TFStateBackendBucket Name"
    
    - Name: "WORKSHOP_GIT_URL"
      Type: "PLAINTEXT"
      Value: "https://github.com/aws-samples/fleet-management-on-amazon-eks-workshop"
    
    - Name: "WORKSHOP_GIT_BRANCH"
      Type: "PLAINTEXT"
      Value: "v1.0.2"
    
    - Name: "FORCE_DELETE_VPC"
      Type: "PLAINTEXT"
      Value: "true"
    
    - Name: "SPOKE"
      Type: "PLAINTEXT"
      Value: "staging"

Project_Spoke_Prod:
  Name: "EKS-Spoke-Prod-Stack-Deploy"
  Description: "Deploy EKS Production spoke cluster"
  
  EnvironmentVariables:
    # Same as staging except:
    - Name: "SPOKE"
      Type: "PLAINTEXT"
      Value: "prod"
  
  BuildSpec: |
    version: 0.2
    phases:
      pre_build:
        commands:
          - yum install -y gettext
          - curl --silent --location "https://get.helm.sh/helm-v3.9.2-linux-amd64.tar.gz" | tar xz -C /tmp
          - mv /tmp/linux-amd64/helm /usr/local/bin
          - chmod +x /usr/local/bin/helm
          - sudo yum install -y yum-utils
          - sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo
          - sudo yum -y install terraform
      
      build:
        commands:
          - set -x
          - set -e
          - aws configure set cli_pager ""
          - ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
          - BUCKET_NAME=${TFSTATE_BUCKET_NAME}
          - BASE_DIR=${CODEBUILD_SRC_DIR}
          - WORKSHOP_GIT_URL=${WORKSHOP_GIT_URL:-https://github.com/aws-samples/fleet-management-on-amazon-eks-workshop}
          - WORKSHOP_GIT_BRANCH=${WORKSHOP_GIT_BRANCH:-v1.0.2}
          - env | sort
          - git clone $WORKSHOP_GIT_URL $BASE_DIR || true
          - cd $BASE_DIR
          - git checkout $WORKSHOP_GIT_BRANCH
          - cd -
          - |
            cat << EOT > $BASE_DIR/terraform/spokes/backend_override.tf
            terraform {
              backend "s3" {
                bucket         = "$BUCKET_NAME"
                key            = "spokes/terraform.tfstate"
                region         = "$AWS_REGION"
                workspace_key_prefix = "spokes"
              }
            }
            EOT
          - mkdir -p ~/.ssh
          - |
            if [[ $REQUESTED_ACTION == 'Delete' ]]; then
              DEBUG=1 $BASE_DIR/terraform/spokes/destroy.sh ${SPOKE}
            else
              DEBUG=1 $BASE_DIR/terraform/spokes/deploy.sh ${SPOKE}
            fi
      
      post_build:
        commands:
          - echo ">>> build status $CODEBUILD_BUILD_SUCCEEDING "
```

</details>

<details>
<summary><b>MODULE: codebuild (EventBridge Rules)</b></summary>

## EventBridge Rules for Build Completion

```yaml
EventBridgeRule_BuildComplete:
  Description: "Build complete"
  State: "ENABLED"
  
  EventPattern:
    source: ["aws.codebuild"]
    detail-type: ["CodeBuild Build State Change"]
    detail:
      build-status: ["SUCCEEDED", "FAILED", "STOPPED"]
      project-name: ["CodeBuild Project Name"]
  
  Targets:
    - Id: "Target0"
      Arn: "ReportBuildLambda ARN"

LambdaPermission:
  Action: "lambda:InvokeFunction"
  FunctionName: "ReportBuildLambda ARN"
  Principal: "events.amazonaws.com"
  SourceArn: "EventBridge Rule ARN"
```

**Note:** Create one rule for each CodeBuild project (4 total: GitOps, Hub, Staging, Prod)

</details>

<details>
<summary><b>MODULE: eks-orchestrator (Cluster Names & Access)</b></summary>

## EKS Cluster Configuration

```yaml
ClusterNames:
  Hub: "fleet-hub-cluster"
  Staging: "fleet-spoke-staging"
  Prod: "fleet-spoke-prod"

EKSAccessEntries:
  Hub:
    ClusterName: "fleet-hub-cluster"
    PrincipalArn: "ParticipantAssumedRoleArn (Parameter)"
    AccessPolicies:
      - PolicyArn: "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
        AccessScope:
          Type: "cluster"
  
  Staging:
    ClusterName: "fleet-spoke-staging"
    PrincipalArn: "ParticipantAssumedRoleArn (Parameter)"
    AccessPolicies:
      - PolicyArn: "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
        AccessScope:
          Type: "cluster"
  
  Prod:
    ClusterName: "fleet-spoke-prod"
    PrincipalArn: "ParticipantAssumedRoleArn (Parameter)"
    AccessPolicies:
      - PolicyArn: "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
        AccessScope:
          Type: "cluster"
```

**Dependencies:**
- Access entries depend on EKS clusters existing
- EKS clusters are created by CodeBuild projects
- Must wait for CodeBuild to complete before creating access entries

</details>

<details>
<summary><b>MODULE: eks-orchestrator (SSM Document - Setup Git)</b></summary>

## SSM Document: Setup Git Repositories

```yaml
SSM_SetupGit:
  Name: "SetupGit"
  DocumentType: "Command"
  DocumentFormat: "YAML"
  TargetType: "/AWS::EC2::Instance"
  UpdateMethod: "NewVersion"
  
  Content:
    schemaVersion: "2.2"
    description: "Setup Git"
    parameters: {}
    
    mainSteps:
      - action: "aws:runShellScript"
        name: "SetupGit"
        inputs:
          runCommand:
            - 'sudo su - ec2-user -c ''GITOPS_DIR=/home/ec2-user/environment/gitops-repos ~/environment/fleet-management-on-amazon-eks-workshop/setup-git.sh'''

SSM_SetupGitAssociation:
  AssociationName: "SetupGitAssociation"
  Name: "SetupGit"
  Targets:
    - Key: "tag:aws:cloudformation:stack-name"
      Values:
        - "fleet-workshop"
        - "fleet-workshop-team-stack"
```

**Dependencies:**
- Runs after EKS Hub cluster is deployed
- Executes script from workshop Git repository

</details>

<details>
<summary><b>PARAMETERS & OUTPUTS</b></summary>

## CloudFormation Parameters

```yaml
Parameters:
  ParticipantAssumedRoleArn:
    Type: "String"
    Description: "ARN of participant assumed role for EKS access"
    # Example: "arn:aws:iam::123456789012:role/WorkshopParticipantRole"
  
  SsmParameterValue_AL2023_AMI:
    Type: "AWS::SSM::Parameter::Value<AWS::EC2::Image::Id>"
    Default: "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-6.1-x86_64"
```

## CloudFormation Outputs

```yaml
Outputs:
  IdeUrl:
    Description: "URL to access the IDE"
    Value: "https://${CloudFrontDistributionDomainName}"
  
  IdePassword:
    Description: "Password for IDE access"
    Value: "From PasswordExporter Lambda Custom Resource"
```

</details>

<details>
<summary><b>CONSTANTS & FIXED VALUES</b></summary>

## Fixed Constants

```yaml
Constants:
  CodeServerVersion: "4.93.1"
  
  WorkshopRepository:
    Url: "https://github.com/aws-samples/fleet-management-on-amazon-eks-workshop"
    Branch: "v1.0.2"
  
  ClusterNames:
    Hub: "fleet-hub-cluster"
    Staging: "fleet-spoke-staging"
    Prod: "fleet-spoke-prod"
  
  GiteaConfig:
    Image: "gitea/gitea:1.22-rootless"
    ContainerHttpPort: 3000
    HostHttpPort: 9999
    ContainerSshPort: 2222
    HostSshPort: 2222
    Username: "workshop-user"
    Email: "workshop-user@example.com"
    DatabaseType: "sqlite3"
  
  ToolVersions:
    Helm: "v3.10.1"
    Terraform: "1.9.3"
    Go: "1.23.1"
    ArgoCD: "v2.12.2"
    EksNodeViewer: "v0.7.1"
  
  CloudFront:
    CachePolicyId: "4135ea2d-6df8-44a3-9df3-4b5a84be39ad"  # CachingDisabled
    OriginRequestPolicyId: "216adef6-5c7f-47e4-b989-5492eafa07d3"  # AllViewer
    CloudFrontPrefixListName: "com.amazonaws.global.cloudfront.origin-facing"
  
  Caddy:
    CodeServerBindAddress: "127.0.0.1:8889"
    GiteaBindAddress: "127.0.0.1:9999"
    HttpPort: 80
```

</details>

<details>
<summary><b>DEPLOYMENT ORDER & DEPENDENCIES</b></summary>

## Resource Deployment Order

```yaml
DeploymentOrder:
  Phase_1_Foundation:
    - VPC
    - Subnet (Public)
    - InternetGateway
    - RouteTable
    - RouteTableAssociation
    - SecurityGroup (IDE)
  
  Phase_2_IAM:
    - SharedRole
    - InstanceProfile
    - Lambda Roles (5 roles)
  
  Phase_3_Storage_Secrets:
    - S3 Bucket (Terraform State)
    - Secrets Manager (IDE Password)
  
  Phase_4_Lambda:
    - PrefixListLookupLambda
    - BootstrapTriggerLambda
    - PasswordExporterLambda
    - StartBuildLambda (x4 - one per CodeBuild project)
    - ReportBuildLambda (x4 - one per CodeBuild project)
  
  Phase_5_Compute:
    - EC2 Instance (IDE)
    - CloudFront Distribution
    - CloudWatch Log Group
  
  Phase_6_SSM:
    - SSM Bootstrap Document
    - SSM Bootstrap Custom Resource (triggers bootstrap)
    - CloudFormation WaitCondition (waits for bootstrap)
  
  Phase_7_CodeBuild:
    - CodeBuild Project: GitOps
    - CodeBuild Project: Hub
    - CodeBuild Project: Staging
    - CodeBuild Project: Prod
  
  Phase_8_EventBridge:
    - EventBridge Rule: GitOps Build Complete
    - EventBridge Rule: Hub Build Complete
    - EventBridge Rule: Staging Build Complete
    - EventBridge Rule: Prod Build Complete
    - Lambda Permissions (x4)
  
  Phase_9_EKS_Orchestration:
    - Custom Resource: Trigger GitOps Build
    - Custom Resource: Trigger Hub Build (depends on GitOps)
    - Custom Resource: Trigger Staging Build (depends on GitOps)
    - Custom Resource: Trigger Prod Build (depends on GitOps)
  
  Phase_10_EKS_Access:
    - EKS Access Entry: Hub (depends on Hub deployment)
    - EKS Access Entry: Staging (depends on Staging deployment)
    - EKS Access Entry: Prod (depends on Prod deployment)
  
  Phase_11_Final_Config:
    - SSM Document: Setup Git
    - SSM Association: Setup Git (depends on Hub deployment)

CriticalDependencies:
  BootstrapWaitCondition:
    WaitsFor: "SSM Bootstrap to complete on EC2"
    Timeout: "1800 seconds (30 minutes)"
  
  CodeBuildTriggers:
    GitOps: "Triggered after IDE bootstrap completes"
    Hub: "Triggered after GitOps completes"
    Staging: "Triggered after GitOps completes"
    Prod: "Triggered after GitOps completes"
  
  EKSAccessEntries:
    Hub: "Created after Hub cluster exists"
    Staging: "Created after Staging cluster exists"
    Prod: "Created after Prod cluster exists"
```

</details>