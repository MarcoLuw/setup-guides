output "msk_bootstrap_brokers_tls" { value = module.msk.bootstrap_brokers_tls }
output "msk_arn" { value = module.msk.msk_arn }
output "bastion_ip" { value = module.bastion.public_ip }
output "vpc_id" { value = module.vpc.vpc_id }