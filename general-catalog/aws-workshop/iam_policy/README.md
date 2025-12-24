## Case 1: Resource-based policy
### Simple

### Advanced
#### Requirement 
Update your bucket policy so that principals in the same account must also have an IAM identity-based allow for `s3:GetObject`.

#### Meaning
- You don’t want the bucket policy itself to give unconditional access to your own account’s users.
- You only want cross-account principals in the org to be granted by this bucket policy.
- Your own account’s principals should be handled by IAM policies only.

#### Net Effect
- Cross-account access: Allowed by the bucket policy
- Same-account access: Explicitly excluded. They must rely on IAM identity-based policies

---
## Case 2: Service control policy (SCP)

---
## Case 3: Identity-based policy
### Simple
- Separate resource `arn:aws:ec2:*:*:instance/*` since the condition key `ec2:InstanceType` is specific to `instance` resources.

### Advanced
- Similar to the **Simple**, separate resource `arn:aws:ec2:*:*:subnet/*`

---
## Case 4: Confused deputy problem
### Policy type used: Resource-based policy

1. Use case: confused duputy problem is a security issue where an entity that doesn't have permission to perform an action can coerce a more-privileged entity to perform the action
2. Example: S3 and an external AWS account
- You own Account A, which has an S3 bucket.
- You allow AWS service X (say, AWS Lambda or CodeBuild) to access that S3 bucket.
- Another account, Account B (the attacker), also uses the same service X.
- If the service X can be “confused” — e.g., it doesn’t know which account’s request is which — it might accidentally access your S3 bucket on behalf of Account B.

---
## Case 5: Lambda function in VPC
- Policy type used: Service control policy (SCP)
1. Use case: Lambda function egress traffic has its VPC network address space
2. Example:

---
## Case 6: Allow principal access to resource with tags
- Policy type used: Identity-based policy
1. Use case: Allow the principal to access resources if principal has specific tags
2. Example: design the identity-based policy to allow principals to access S3 buckets when they have the same department tag as those attached to the bucket.