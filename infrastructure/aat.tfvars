#================================================================================================
# Azure Monitor
#================================================================================================
civil_service_alert_slack_email_secret_name = "civil-service-alert-slack-group-email"

monitor_action_group = {
  "aat-civil-service-slack-alert" = {
    short_name = "cvlsr-aat"
  }
}

monitor_cmc_db_metric_alerts = {
  "aat-cmc-db-cpu-warning" = {
    description  = "AAT CMC DB CPU utilization >= 70% for 5 mins"
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
    action = [{ action_group_name = "aat-civil-service-slack-alert" }]
  },
  "aat-cmc-db-cpu-critical" = {
    description  = "AAT CMC DB CPU utilization >= 85% for 5 mins"
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
    action = [{ action_group_name = "aat-civil-service-slack-alert" }]
  },
  "aat-cmc-db-storage-warning" = {
    description  = "AAT CMC DB disk usage >= 80%"
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
    action = [{ action_group_name = "aat-civil-service-slack-alert" }]
  },
  "aat-cmc-db-storage-critical" = {
    description  = "AAT CMC DB disk usage >= 90%"
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
    action = [{ action_group_name = "aat-civil-service-slack-alert" }]
  },
  "aat-cmc-db-memory-warning" = {
    description  = "AAT CMC DB memory usage >= 85%"
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
    action = [{ action_group_name = "aat-civil-service-slack-alert" }]
  },
  "aat-cmc-db-memory-critical" = {
    description  = "AAT CMC DB memory usage >= 95%"
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
    action = [{ action_group_name = "aat-civil-service-slack-alert" }]
  }
}
