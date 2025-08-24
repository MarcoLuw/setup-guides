# Final AWS Architecture Design (3-Tier App)

**Goal:** combine my first idea with all best-practice improvements into one sane, production-ready design. I’ll split it into big components so I can research each part deeper later.

**Related documentation:** 
* [My first business's painpoint thought](blog-style.md)
* [Implementation documnent](implementation.md)
---

## 0. Scope & Principles I’ll stick to

* **Scope:** AWS-only, 3-tier web app (frontend, API backend, database) with object storage and cache.
* **Principles:** AWS Well-Architected (Security, Reliability/HA, Performance, Cost, Ops, Sustainability), everything as code (Terraform), least privilege, managed/serverless where it helps.

---

## 1. Compute Component

### My initial idea

* Frontend (TypeScript) on a single EC2, manually set up.
* Backend (Python/FastAPI) on a single EC2, manually set up.

### What’s wrong with that?

* Single EC2 = **SPOF**, manual config = **snowflake** servers, scaling is painful, patching is manual.

### Improved points (best practice)

* **Frontend:**
  * Static site on **S3 Static Website Hosting** + **CloudFront** for global CDN.
  * Optional **Lambda\@Edge/CloudFront Functions** for lightweight edge logic (security headers, redirects).

* **Backend:**
  * Containerize app, push to **ECR**.
  * Run on **ECS with Fargate** (no servers) behind an **Application Load Balancer (ALB)**.
  * **Auto Scaling** ECS services on CPU/mem/RequestCount/TargetTracking.
  * **Service mesh** needs? If yes later, consider **App Mesh**; else keep simple.
* **Build artifacts:** keep OCI images in **ECR**, static build artifacts in **S3**.
* **Session/state:** strictly **stateless** app; use **ElastiCache (Redis)** or signed JWTs for sessions; never store state on containers.

### Final decision & why

* **S3 + CloudFront** for frontend → cheaper, faster, zero servers.
* **ECS Fargate + ALB + ECR** for backend → managed, scalable, Multi-AZ by default.
* This removes SPOFs, supports autoscaling, and is Terraform-friendly.

### Open questions I’ll answer later

* Do I need **EKS** instead (if I require k8s ecosystem)? For now, **no**—ECS Fargate is enough.
* Any **background workers/queues**? If yes, add **SQS** + separate ECS service or **Lambda**.

---

## 2. Storage & Data Component

### My initial idea

* Database: **RDS PostgreSQL**
* Object storage: **S3**
* Cache: **ElastiCache**

### Improved points (best practice)

* **Relational DB:**

  * **RDS PostgreSQL Multi-AZ** (or **Aurora PostgreSQL** if I need better performance/replicas/global).
  * **Automated backups + PITR**, **KMS at-rest encryption**, **TLS in transit**.
  * **RDS Proxy** to stabilize connection storms from ECS.
  * **Parameter group** tuning via Terraform; performance insights enabled in non-prod/prod.
* **Object Storage:**

  * **S3** with **bucket policies**, **least privilege IAM**, **KMS** encryption, **S3 Block Public Access**.
  * **Lifecycle policies** (e.g., **Intelligent-Tiering** or Glacier for cold data).
  * If cross-region DR needed: **CRR** (Cross-Region Replication).
* **Cache:**

  * **ElastiCache Redis** (cluster mode disabled or enabled depending on scale) in **private subnets**, KMS if supported, TLS where applicable.
  * Use it for session stores, hot data, and rate limiting.

### Final decision & why

* **RDS PG Multi-AZ + RDS Proxy**, **S3 (encrypted & lifecycle)**, **ElastiCache Redis** → standard 3-tier best practice with durability, performance, and cost controls.

### Open questions I’ll answer later

* Aurora vs RDS? I’ll baseline with **RDS PG Multi-AZ** and revisit once load/replica needs grow.

---

## 3. Networking Component

### My initial idea

* VPC, public/private subnets, IGW, NAT GW, route tables, security groups.

### Improved points (best practice)

* **VPC layout (at least 3 AZs):**

  * **Public subnets:** ALB, NAT Gateways, bastion (if I really need SSH; ideally I do **not**).
  * **Private subnets:** ECS tasks/services, RDS, ElastiCache.
  * (Optional) **Isolated subnets**: highly sensitive data stores with no egress.
* **Egress/Ingress:**

  * **Internet Gateway** for ALB; **NAT Gateways** per AZ for private egress (or carefully shared with cost trade-off).
  * **VPC Endpoints (Interface/Gateway)** for S3, ECR, CloudWatch Logs, Secrets Manager, SSM → **no public egress for AWS APIs**.
* **Routing & DNS:**

  * **Route 53** public/ private hosted zones; ALB with custom domain + ACM certs.
  * Health checks at ALB + Route 53 if I ever do multi-region.
* **No SSH** into hosts: use **SSM Session Manager** instead of bastions where possible.

### Final decision & why

* Multi-AZ VPC with **public (edge) + private (app/data)** subnets, **VPC endpoints**, **Route 53**, **ACM**, **no direct SSH** → secure, scalable, AWS-native.

### Open questions I’ll answer later

* Do I need **Transit Gateway** (multi-VPC/hybrid)? Not now; keep simple.

---

## 4. Security Component

### My initial idea

* IAM roles/policies, KMS, Secrets Manager/Parameter Store, TLS, WAF/Shield/GuardDuty/Inspector.

### Improved points (best practice)

* **Identity & Access:**

  * **IAM roles** for ECS tasks (task roles), not instance roles; strict **least privilege**.
  * **OIDC/JWT** auth at app level; consider **Cognito** if I want managed user auth later.
* **Secrets & Config:**

  * **AWS Secrets Manager** for DB creds (automatic rotation), **SSM Parameter Store** for non-secret configs.
  * **Refer secrets via task definitions**, never bake into images or env files.
* **Encryption:**

  * **KMS** for RDS, S3, EBS (if any), ElastiCache (where supported).
  * **TLS everywhere**: ALB HTTPS (ACM cert), TLS to RDS/Redis.
* **Edge/App protection:**

  * **AWS WAF** on ALB/CloudFront (managed rules: SQLi/XSS, IP reputation, rate limit).
  * **AWS Shield Standard** (included) or **Shield Advanced** if public-facing with high risk.
* **Threat detection & posture:**

  * **GuardDuty** (account + VPC flow + DNS + EKS/ECS logs where supported).
  * **Inspector** for container image scans/ECR and Lambda if used.
  * **Security Hub** to aggregate, **AWS Config** for conformance packs & drift.
* **Access to instances/containers:**

  * **SSM** only, audit with **CloudTrail** + **CloudWatch Logs**.

### Final decision & why

* Full stack of **least privilege + encryption + WAF/Shield + continuous detection (GuardDuty/Inspector/Security Hub)** → reduces breach blast radius and raises the bar for attackers.

### Open questions I’ll answer later

* Do I need customer-managed KMS keys vs AWS-managed? Start with **CMKs** where rotation and key policies matter (DB/data).

---

## 5. High Availability (HA) & Resilience Component

### My initial idea

* Multi-AZ? Backup? Elastic Disaster Recovery?

### Improved points (best practice)

* **HA within a region:**

  * **ALB** spread across AZs, **ECS services** with tasks in **at least 2–3 AZs**.
  * **RDS Multi-AZ**, **ElastiCache Multi-AZ** with automatic failover.
  * **CloudFront** to absorb edge spikes and route users optimally.
* **Backups & DR:**

  * **AWS Backup** for RDS (and EFS if used), define **backup plans**, **vaults**, **lifecycles**.
  * **RPO/RTO** defined per tier; if strict, consider **Aurora Global Database**.
  * **S3 CRR** for critical buckets.
  * For full-app DR: document runbooks or use **AWS Elastic Disaster Recovery (DRS)** for EC2-based components (not needed for Fargate but useful for legacy).
* **Fault isolation:**

  * **Circuit breakers/retries** at app layer, **graceful degradation** when cache/DB is down.

### Final decision & why

* **Multi-AZ across all tiers**, backups with defined policies, and **CloudFront** + **RPO/RTO** targets → predictable recovery and minimal downtime.

### Open questions I’ll answer later

* **Multi-region active-active?** Only if business RTO/RPO requires it; otherwise stick to one region with robust HA.

---

## 6. Performance & Latency Component

### My initial idea

* CloudFront, Global Accelerator, reduce response time, X-Ray (maybe).

### Improved points (best practice)

* **Edge & caching:**

  * **CloudFront** in front of S3 and (optionally) ALB for dynamic content caching.
  * **ElastiCache Redis** for hot keys, rate limiting, and expensive queries.
* **Connection management:**

  * **RDS Proxy** for DB connection pooling from ECS tasks.
* **Global users:**

  * Consider **AWS Global Accelerator** if I need consistent anycast IPs and faster cross-region routing to ALB.
* **App insights:**

  * **AWS X-Ray** for distributed tracing; identify slow hops.

### Final decision & why

* **CloudFront + Redis + RDS Proxy + X-Ray** gives me low latency, stable DB connections, and visibility into hotspots.

### Open questions I’ll answer later

* Do I need **GA** now? I’ll measure first; add GA if global latency is an issue.

---

## 7. Observability & Operations Component

### My initial idea

* CloudWatch, “research more.”

### Improved points (best practice)

* **Metrics & alarms:**

  * **CloudWatch** dashboards + alarms (ALB 5xx, target response time; ECS CPU/mem; RDS CPU/connections/lag; Redis engine CPU/evictions; S3 4xx/5xx via CloudFront).
* **Logs:**

  * **CloudWatch Logs** for ALB access logs (via S3 + Athena optional), ECS task logs (FireLens if I need JSON/ship to OpenSearch), RDS enhanced monitoring.
* **Tracing:**

  * **AWS X-Ray** SDK in FastAPI; sample traces.
* **Config & audit:**

  * **AWS Config** rules, **CloudTrail** to S3 with retention + Lake for queries.
  * **Security Hub** to centralize security findings.
* **SLOs/SLIs:**

  * Define latency/availability/error-rate SLOs; alarms align to them.

### Final decision & why

* Unified **CloudWatch + X-Ray + Config + CloudTrail** → operational excellence, faster incident response.

### Open questions I’ll answer later

* Need **OpenSearch** for log analytics at scale? Start with CW Logs Insights; grow if needed.

---

## 8. Cost Optimization Component

### My initial idea

* Savings Plans? Choose right services. Monitor.

### Improved points (best practice)

* **Right services first:**

  * **Fargate** to avoid idle EC2; **S3 Intelligent-Tiering**; **RDS** instance sizing with performance insights; **ElastiCache** right sizing.
* **Pricing levers:**

  * **Compute Savings Plans** for ECS Fargate; **RDS Reserved Instances** (or Aurora I/O-Optimized if Aurora chosen).
  * **NAT GW** per AZ vs centralized — weigh cost vs risk; minimize egress with **VPC Endpoints**.
  * **CloudFront** offload reduces ALB/Data Transfer Out costs.
* **Governance:**

  * **AWS Budgets**, **Cost Explorer**, and **anomaly detection**; tag everything for cost allocation.

### Final decision & why

* Use **managed/serverless** to reduce ops cost, then apply **Savings Plans/Reservations** once usage stabilizes, and **budget alarms** to prevent surprises.

### Open questions I’ll answer later

* When is usage stable enough for 1- or 3-year commitments? I’ll monitor for a few billing cycles.

---

## 9. Delivery, Security of Delivery & IaC Component (brief but necessary)

### My initial idea

* Manual setup (uh-oh).

### Improved points (best practice)

* **Terraform** for all resources; separate **environments** (dev/stage/prod) with workspaces or directories; use **modules** per component (networking, compute, data, security).
* **Pipelines:** Build (Docker) → test → push to **ECR** → deploy **ECS** via Terraform or progressive delivery tool.
* **No long-lived keys:** CI/CD uses **OIDC** to assume roles in AWS (no stored secrets).
* **Policy as code:** **Terraform + AWS Config conformance packs**; pre-commit checks.

### Final decision & why

* **Everything as code** + secure CI access via **OIDC** → reproducible and auditable deployments.

### Open questions I’ll answer later

* Add **Canary/Blue-Green**? ECS supports blue/green with **CodeDeploy**; decide when I set SLOs.

---

## 10. Final “Wiring Diagram” in words (so I don’t forget)

* **Users → CloudFront** (HTTPS, ACM cert)

  * Static site from **S3** (private, OAC/origin access control).
  * Dynamic path routes to **ALB** origin.
* **ALB → ECS (Fargate) services** across **3 AZs**, tasks in **private subnets**, target groups with health checks.
* **ECS tasks** pull images from **ECR**, fetch secrets from **Secrets Manager/SSM**, emit logs to **CloudWatch Logs/X-Ray**.
* **ECS → RDS PostgreSQL (Multi-AZ)** via **RDS Proxy**, and **ElastiCache Redis** for caching/sessions.
* **VPC** with public subnets (ALB, NAT), private subnets (ECS/RDS/Redis), **VPC Endpoints** for S3/ECR/Logs/Secrets/SSM.
* **Security:** WAF on CloudFront/ALB, Shield Std, SGs least-privilege (ALB→ECS, ECS→DB/Redis), KMS everywhere, IAM task roles only.
* **Ops:** CloudWatch dashboards/alarms, AWS Backup plans, CloudTrail + Config + Security Hub + GuardDuty + Inspector.
* **Cost:** Savings Plans/RIs when stable, S3 lifecycle/Intelligent-Tiering, minimize NAT with endpoints.

---

## 11. Checklist of Improved Points (explicit keywords for my deep-dive)

1. **Frontend:** S3 static hosting + CloudFront, OAC, ACM, TLS
2. **Backend:** ECS **Fargate** + **ALB** + **ECR**, stateless services, auto scaling
3. **Database:** **RDS PG Multi-AZ** (or **Aurora** later), **PITR**, **RDS Proxy**, KMS, TLS
4. **Cache:** **ElastiCache Redis** in private subnets, Multi-AZ, KMS/TLS
5. **Networking:** **3-AZ VPC**, public+private(+isolated) subnets, **NAT GW**, **VPC Endpoints**, Route 53
6. **Security:** **IAM task roles**, **Secrets Manager/SSM**, **WAF**, **Shield**, **KMS**, **CloudTrail**, **no SSH (SSM)**
7. **HA/Resilience:** Multi-AZ for all tiers, **AWS Backup**, **S3 CRR** (if needed), defined **RTO/RPO**, optional **DRS**
8. **Performance:** **CloudFront**, **RDS Proxy**, **ElastiCache**, optional **Global Accelerator**, **X-Ray**
9. **Observability:** **CloudWatch** metrics/alarms/logs, **X-Ray**, **Config**, **Security Hub**, **GuardDuty**, **Inspector**
10. **Cost:** **Compute Savings Plans**, **RDS RIs**, **S3 Intelligent-Tiering**, **Budgets**, **Cost Explorer**, minimize NAT egress
11. **IaC & Delivery:** Terraform modules/environments, CI with **OIDC**, blue-green optional via **CodeDeploy**

---

## 12. Things I’ll explicitly *not* do (on purpose)

* No single EC2 boxes for core app tiers (too fragile).
* No public S3 buckets (use OAC).
* No secrets in env files or images (Secrets Manager/SSM only).
* No SSH/bastions by default (SSM Session Manager).

---
**References:**
* [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)