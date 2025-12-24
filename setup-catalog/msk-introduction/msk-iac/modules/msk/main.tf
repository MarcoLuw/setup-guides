resource "aws_cloudwatch_log_group" "broker_logs" {
  name              = "/aws/msk/${var.cluster_name}/broker-logs"
  retention_in_days = 30
}


resource "aws_msk_cluster" "this" {
  cluster_name           = var.cluster_name
  kafka_version          = var.kafka_version
  number_of_broker_nodes = length(var.subnets)


  broker_node_group_info {
    instance_type   = var.broker_instance_type
    client_subnets  = var.subnets
    security_groups = [var.security_group_id]
    storage_info {
      ebs_storage_info { volume_size = var.ebs_gb }
    }
  }


  encryption_info {
    encryption_in_transit {
      client_broker = "TLS"
      in_cluster    = true
    }
  }

  client_authentication {
    sasl {
      iam = true
    }
  }


  open_monitoring {
    prometheus {
      jmx_exporter {
        enabled_in_broker = true
      }
      node_exporter {
        enabled_in_broker = true
      }
    }
  }


  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = true
        log_group = aws_cloudwatch_log_group.broker_logs.name
      }
    }
  }


  tags = { Environment = "prod" }
}