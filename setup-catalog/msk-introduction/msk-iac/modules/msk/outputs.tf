output "msk_arn" { value = aws_msk_cluster.this.arn }
output "bootstrap_brokers_tls" { value = aws_msk_cluster.this.bootstrap_brokers_tls }
output "zookeeper_connect_string" { value = aws_msk_cluster.this.zookeeper_connect_string }