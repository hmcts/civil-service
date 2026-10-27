send_grid_subscription = "8999dec3-0104-4a27-94ee-6588559729d1"

custom_alerts_enabled = 1

ccd_service_bus_status = "Disabled"

ccd_service_bus_filter_rule = "jurisdiction_id IN ('IMPOSSIBLE_VALUE')"

#================================================================================================
# Azure Monitor
#================================================================================================
civil_service_alert_slack_email_secret_name = "civil-service-alert-slack-group-email"

monitor_action_group = {
  "prod-civil-service-slack-alert" = {
    short_name = "cvlsr-prod"
  }
}

monitor_cmc_db_metric_alerts = {
  "prod-cmc-db-cpu-warning" = {
    description = "Five-minute average CPU utilization >= 70%"
    severity    = 2
    enabled     = true
    frequency   = "PT1M"
    window_size = "PT5M"
    criteria = [{
      operator        = "GreaterThanOrEqual"
      threshold       = 70
      metric_namespace = "Microsoft.DBforPostgreSQL/flexibleServers"
      metric_name      = "cpu_percent"
      aggregation     = "Average"
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-cpu-critical" = {
    description  = "Five-minute average CPU utilization >= 85%"
    severity     = 1
    enabled      = true
    frequency    = "PT1M"
    window_size  = "PT5M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 85
      metric_namespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metric_name           = "cpu_percent"
      aggregation          = "Average"
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-storage-warning" = {
    description  = "Production CMC DB disk usage >= 80%"
    severity     = 2
    enabled      = true
    frequency    = "PT5M"
    window_size  = "PT15M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 80
      metric_namespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metric_name           = "storage_percent"
      aggregation          = "Maximum"
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-storage-critical" = {
    description  = "Production CMC DB disk usage >= 90%"
    severity     = 0
    enabled      = true
    frequency    = "PT5M"
    window_size  = "PT15M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 90
      metric_namespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metric_name           = "storage_percent"
      aggregation          = "Maximum"
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-memory-warning" = {
    description  = "Production CMC DB memory usage >= 85%"
    severity     = 2
    enabled      = true
    frequency    = "PT1M"
    window_size  = "PT5M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 85
      metric_namespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metric_name           = "memory_percent"
      aggregation          = "Average"
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-memory-critical" = {
    description  = "Production CMC DB memory usage >= 95%"
    severity     = 0
    enabled      = true
    frequency    = "PT1M"
    window_size  = "PT5M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 95
      metric_namespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metric_name           = "memory_percent"
      aggregation          = "Average"
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  }
}
