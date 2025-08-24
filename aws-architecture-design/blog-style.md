# My first business's painpoint thought

**Problem-solving thinking:**
Before starting with Terraform designing, we need to think about the required **architecture**.

## Target: AWS for System Deployment and Hosting

Which components should be considered?

### Application Architecture
- **Question**: What is the architecture of the app? Options include:
  - 3-tier app
  - Service-oriented
  - Microservice
  - Composite architecture (e.g., 3-tier + microservice)
- **Assumption**: We will manage a **3-tier app**.

### Components and AWS Services
For the simplest approach (ensuring the app can run at least):
- **Frontend (TypeScript)**:
  - One EC2 instance to host
  - Manually set up/configure the instance
  - Start the project (e.g., `npm run`)
- **Backend (Python)**:
  - One EC2 instance to host
  - Manually set up/configure the instance
  - Start the project (e.g., `fastapi run`)
- **Database**:
  - RDS (PostgreSQL)
- **Object Storage**:
  - S3
- **Caching**:
  - ElastiCache

### Initial Assessment
With this configuration, the app is ready to serve customer traffic. But is it as simple as we thought? Are there any risks or concerns we need to address?

**Self-Answer**: Yes, there are concerns. We must consider the following (and possibly more):
- **Business Objectives**: The main concern is **profit** (high profit = low expense + high revenue).
  - There may be trade-offs between saving costs and increasing revenue, but we’ll address them independently.
  - **Cost-Effective System**:
    - **Optimizing AWS Service Purchases**:
      - Explore AWS plans (e.g., EC2 Savings Plans)
      - Use appropriate services, avoiding overkill (but what is "enough"?)
    - **Reducing Risks of Financial Loss**:
      - **Security Risks**:
        - Data leaks (e.g., business secrets revealed)
        - Sensitive information leaks (e.g., API keys, managed keys)
      - **System Downtime**:
        - Disasters (e.g., power outage, fire)
        - Software issues (e.g., crashes, bugs)
        - Hardware issues (e.g., memory or disk failure; less relevant with AWS services)
        - Traffic/Performance issues (e.g., overload, traffic spikes, insufficient resources like OOM or CPU shortages)
        - Cyberattacks (e.g., DDoS, brute-force, CSRF, SQL injection, XSS)
  - **High Revenue**:
    - From a system operations perspective, high revenue means providing an excellent user experience, including:
      - Fast response time
      - *(Note: Additional ideas for this section are needed)*

- **Client-Side Considerations**:
  - Infrastructure management happens behind the scenes, so we can exclude user concerns.

## Comprehensive Solutions
To make the application more **robust, secure, highly available, and resilient** (i.e., runs well, fast, secure, minimizes downtime, and recovers quickly), let’s address the issues step-by-step while defining technical terms.

### 1. Purchase Optimization
- **Problem**: AWS buying plans
- **Solution**: 
  - Read documentation, analyze pros and cons of each plan, and make an informed decision (requires experience and heuristics).
- **Problem**: Accurate service utilization
- **Solution**: 
  - Predefine which service to use among similar options (e.g., RDS vs. Aurora DB, EC2 vs. ECS vs. Lambda, ElastiCache vs. Memcached).
  - **Sub-Issue**: This is a trial-and-error process requiring thorough monitoring and comparison.
  - **Monitoring and Observability**: Use tools like CloudWatch (research more).

### 2. Reducing Financial Loss Risks (System Robustness)
- **Problem**: Security risks
- **Solution**:
  - **Data Leaks**: Restrict access and grant only the least privileges to designated entities.
    - **Networking and Security Patterns**:
      - VPC design
      - Subnets (public and private)
      - Security Groups
      - NAT Gateway (for private subnets to access the internet, outbound only)
      - Internet Gateway (for VPC to internet, inbound and outbound)
      - Route Tables (control traffic flow within the VPC)
    - **Least Privilege Practices**:
      - IAM roles and policies
  - **Sensitive Data**: Follow security best practices.
    - Keys: AWS KMS
    - Secrets: AWS Secrets Manager, AWS Parameter Store
    - Encryption: At-rest encryption, in-transit encryption, TLS, certificates
    - **Sub-Issue**: Are these solutions truly secure?
- **Problem**: Ensure system durability (minimize downtime and recover quickly).
  - **Questions**:
    - How to minimize system downtime? (High availability)
    - How to recover if downtime occurs? (Resilience)
    - How to harden security to prevent disruptions?
- **Solution for High Availability**:
  - **High Availability**: Ability to resist downtime caused by:
    - Resource outages (e.g., overload, traffic spikes, OOM, CPU shortages)
    - Application crashes (e.g., bugs)
  - **AWS Solutions**:
    - Load Balancer (NLB or ELB)
    - Auto Scaling (horizontal/vertical)
    - Group of instances/nodes (EC2/ECS)
    - Multi-AZ deployment
    - Multi-region deployment
- **Solution for Resilience**: Ability to resist or recover from disruptions.
  - **Backup Approach**:
    - AWS Backup
    - Database backups (e.g., Point-in-Time Recovery for databases)
  - **Recovery Approach**:
    - AWS Elastic Disaster Recovery
    - Consider Recovery Time Objective (RTO) and Recovery Point Objective (RPO)
- **Solution for Security (Cyberattacks)**:
  - AWS WAF: Protects against SQL injection, cross-site scripting
  - AWS Shield: Mitigates DDoS attacks
  - AWS GuardDuty: Uses ML for threat detection and AWS account monitoring
  - AWS Inspector: Scans for common vulnerabilities in EC2, container images, and Lambda
- **Final Note**:
  - AWS provides the **AWS Well-Architected Framework**, which defines six pillars for reliable, secure, efficient, cost-effective, and sustainable systems.
  - Use the **AWS Well-Architected Tool** to evaluate workloads against these best practices.

### 3. Optimize Response Time
AWS offers solutions to reduce unnecessary waiting time:
- **Optimizing Network Latency**:
  - AWS Global Accelerator (leverages edge locations)
  - AWS Outposts or AWS Wavelength (for on-premises systems, requires further research)
- **CDN**: CloudFront
- **Monitoring**:
  - CloudWatch
  - AWS X-Ray (traces requests to identify areas of high latency)