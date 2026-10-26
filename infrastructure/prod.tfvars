send_grid_subscription = "8999dec3-0104-4a27-94ee-6588559729d1"

custom_alerts_enabled = 1

ccd_service_bus_status = "Disabled"

ccd_service_bus_filter_rule = "jurisdiction_id IN ('IMPOSSIBLE_VALUE')"

#================================================================================================
# Azure Monitor
#================================================================================================
civil_service_alert_slack_email_secret_name = "civil-service-alert-slack-email"

monitor_action_group = {
  "prod-civil-service-slack-alert" = {
    short_name = "cvlsr-prod"
  }
}

monitor_cmc_db_metric_alerts = {
  "prod-cmc-db-cpu-warning" = {
    description  = "Production CMC DB CPU utilization >= 70% for 5 mins"
    severity     = 2
    enabled      = true
    frequency    = "PT1M"
    autoMitigate = true
    window_size  = "PT5M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 70
      metricNamespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metricName           = "cpu_percent"
      aggregation          = "Average"
      skipMetricValidation = false
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-cpu-critical" = {
    description  = "Production CMC DB CPU utilization >= 85% for 5 mins"
    severity     = 0
    enabled      = true
    frequency    = "PT1M"
    autoMitigate = true
    window_size  = "PT5M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 85
      metricNamespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metricName           = "cpu_percent"
      aggregation          = "Average"
      skipMetricValidation = false
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-storage-warning" = {
    description  = "Production CMC DB disk usage >= 80%"
    severity     = 2
    enabled      = true
    frequency    = "PT5M"
    autoMitigate = true
    window_size  = "PT15M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 80
      metricNamespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metricName           = "storage_percent"
      aggregation          = "Maximum"
      skipMetricValidation = false
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-storage-critical" = {
    description  = "Production CMC DB disk usage >= 90%"
    severity     = 0
    enabled      = true
    frequency    = "PT5M"
    autoMitigate = true
    window_size  = "PT15M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 90
      metricNamespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metricName           = "storage_percent"
      aggregation          = "Maximum"
      skipMetricValidation = false
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-memory-warning" = {
    description  = "Production CMC DB memory usage >= 85%"
    severity     = 2
    enabled      = true
    frequency    = "PT1M"
    autoMitigate = true
    window_size  = "PT5M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 85
      metricNamespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metricName           = "memory_percent"
      aggregation          = "Average"
      skipMetricValidation = false
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  },
  "prod-cmc-db-memory-critical" = {
    description  = "Production CMC DB memory usage >= 95%"
    severity     = 0
    enabled      = true
    frequency    = "PT1M"
    autoMitigate = true
    window_size  = "PT5M"
    criteria = [{
      operator             = "GreaterThanOrEqual"
      threshold            = 95
      metricNamespace      = "Microsoft.DBforPostgreSQL/flexibleServers"
      metricName           = "memory_percent"
      aggregation          = "Average"
      skipMetricValidation = false
    }]
    action = [{ action_group_name = "prod-civil-service-slack-alert" }]
  }
}
