variable "cluster_name" { type = string }
variable "kafka_version" { type = string }
variable "broker_instance_type" { type = string }
variable "ebs_gb" { type = number }
variable "subnets" { type = list(string) }
variable "security_group_id" { type = string }