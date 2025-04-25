# AWS EKS Cluster Setup Guide

This guide outlines the steps to create an Amazon EKS cluster using the AWS Console, set up an ingress controller, apply Horizontal Pod Autoscaling (HPA), and manage multiple AWS accounts for accessing different EKS clusters.

**Reference:** [Provision AWS EKS from the AWS Console](https://mycloudjourney.medium.com/provision-aws-eks-from-the-aws-console-8c253595a546)

## 1. Create EKS Cluster via AWS Console

### Step 1: Select Region
- Choose the desired AWS region for your EKS cluster in the AWS Management Console.

### Step 2: Create EKS Cluster IAM Role
**Why?** The EKS cluster makes calls to AWS services, requiring an IAM role with the `AmazonEKSClusterPolicy`.

**How to Create:**
1. Open the IAM console.
2. Navigate to **Roles** and click **Create Role**.
3. Select **AWS service** under **Trusted entity type**.
4. From the **Use cases for other AWS services** dropdown, choose **EKS**.
5. Select **EKS - Cluster** and click **Next**.
6. Attach the `AmazonEKSClusterPolicy` and proceed to create the role.

### Step 3: Create EKS Cluster Node Role
**Why?** The kubelet on worker nodes makes calls to AWS APIs, requiring an IAM role with specific permissions.

**Required Policies:**
- `AmazonEKSWorkerNodePolicy`
- `AmazonEC2ContainerRegistryReadOnly`
- `CloudWatchLogsFullAccess`
- `AmazonEKS_CNI_Policy`

**How to Create:**
1. Follow the same steps as creating the cluster IAM role but select **EC2** as the trusted entity.
2. Attach the above policies to the role.

### Step 4: Update kubeconfig
To configure `kubectl` to interact with your EKS cluster:
```bash
aws eks --region ap-northeast-2 update-kubeconfig --name eks-cluster-demo
```

## 2. Install eksctl
To manage EKS clusters, install `eksctl`:
```bash
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_Linux_amd64.tar.gz" | tar xz -C /engn001/install
sudo mv /engn001/install/eksctl /usr/local/bin
```

## 3. Create an Ingress for EKS Cluster
An ingress controller is required to route external traffic to services in the EKS cluster. This guide covers both AWS Load Balancer Controller and Nginx Ingress Controller.

**Reference:** [Route application and HTTP traffic with Application Load Balancers](https://docs.aws.amazon.com/eks/latest/userguide/alb-ingress.html)

### Prerequisites
- Ensure at least two subnets per Availability Zone (AZ).
- Tag subnets:
  - Private subnets: `kubernetes.io/role/internal-elb=1`
  - Public subnets: `kubernetes.io/role/elb=1`
- Install an ingress controller (AWS Load Balancer Controller or Nginx Ingress Controller).

### Option 1: AWS Load Balancer Controller
**Reference:** 
- [AWS Load Balancer Controller Documentation](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html)
- [Install AWS Load Balancer Controller with Helm](https://docs.aws.amazon.com/eks/latest/userguide/lbc-helm.html)

#### Prerequisites
- Create an OpenID Connect (OIDC) provider for the EKS cluster if missing.
  - **Why?** To use IAM roles for service accounts (IRSA).
  - **How?** Follow [AWS documentation](https://docs.aws.amazon.com/eks/latest/userguide/enable-iam-roles-for-service-accounts.html).
- Download the IAM policy for the AWS Load Balancer Controller:
  ```bash
  curl -O https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.11.0/docs/install/iam_policy.json
  ```

#### Step 1: Create IAM Role
1. Create an IAM policy:
   ```bash
   aws iam create-policy \
       --policy-name AWSLoadBalancerControllerIAMPolicy \
       --policy-document file://iam_policy.json
   ```
2. Create an IAM service account and attach the policy:
   ```bash
   eksctl create iamserviceaccount \
       --cluster=huyen-eks-cluster \
       --namespace=kube-system \
       --name=aws-load-balancer-controller \
       --role-name AmazonEKSLoadBalancerControllerRole \
       --attach-policy-arn=arn:aws:iam::975050376580:policy/AWSLoadBalancerControllerIAMPolicy \
       --approve
   ```

#### Step 2: Install AWS Load Balancer Controller
1. Install Helm if not already installed:
   ```bash
   sudo dnf update -y
   curl -fsSL https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
   ```
2. Add and update the EKS charts Helm repository:
   ```bash
   helm repo add eks https://aws.github.io/eks-charts
   helm repo update eks
   ```
3. Install the AWS Load Balancer Controller:
   ```bash
   helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
       -n kube-system \
       --set clusterName=huyen-eks-cluster \
       --set serviceAccount.create=false \
       --set serviceAccount.name=aws-load-balancer-controller \
       --set region=ap-southeast-2 \
       --set vpcId=vpc-08dac33ebe4164ad2
   ```

#### Step 3: Verify Installation
```bash
kubectl get deployment -n kube-system aws-load-balancer-controller
```

#### Optional: Delete Resources
- Delete the IAM service account:
  ```bash
  eksctl delete iamserviceaccount \
      --cluster=huyen-eks-cluster \
      --namespace=kube-system \
      --name=aws-load-balancer-controller
  ```
- Uninstall the controller:
  ```bash
  helm uninstall aws-load-balancer-controller -n kube-system
  ```

### Option 2: Nginx Ingress Controller
**Reference:** [Nginx Ingress Controller Documentation](https://kubernetes.github.io/ingress-nginx)

#### Step 1: Install Nginx Ingress Controller
1. Add the Nginx Helm repository:
   ```bash
   helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
   ```
2. Install the controller:
   ```bash
   helm upgrade -i ingress-nginx ingress-nginx/ingress-nginx \
       --version 4.2.3 \
       --namespace kube-system \
       --set controller.service.type=LoadBalancer \
       --set allowSnippetAnnotations=true
   ```
3. Verify the installation:
   ```bash
   kubectl -n kube-system rollout status deployment ingress-nginx-controller
   kubectl get deployment -n kube-system ingress-nginx-controller
   ```

#### Step 2: Delete Nginx Ingress Controller
```bash
helm uninstall ingress-nginx -n kube-system
```

## 4. Apply Horizontal Pod Autoscaling (HPA)

### Step 1: Install Metrics Server
- Check if the metrics server is installed:
  ```bash
  kubectl get deployment -n kube-system metrics-server
  ```
- If not installed, install it using the EKS add-on or Helm.

### Step 2: Set Resource Requests
- Update your deployment to include resource requests (CPU/memory) for pods.

### Step 3: Create HPA Configuration
Create an HPA YAML file to define autoscaling rules based on metrics like CPU utilization.

Example HPA YAML:
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: example-hpa
  namespace: default
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: example-deployment
  minReplicas: 1
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

Apply the HPA:
```bash
kubectl apply -f hpa.yaml
```

# Appendix. Work with Multiple AWS Accounts

### Step 1: List AWS Profiles
```bash
aws configure list-profiles
```

### Step 2: View Profile Details
```bash
aws configure list --profile account-a
```

### Step 3: Check Active AWS Identity
```bash
aws sts get-caller-identity --profile account-a
```

### Step 4: Add a New Account Profile
```bash
aws configure --profile account-b
```

### Step 5: Switch Between AWS Accounts
List clusters for different accounts:
```bash
aws eks list-clusters --profile account-a
aws eks list-clusters --profile account-b
```

### Step 6: Set Default Profile (Temporarily)
```bash
export AWS_PROFILE=account-a
```