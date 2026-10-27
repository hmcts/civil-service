#================================================================================================
# Azure Monitor
#================================================================================================
civil_service_alert_slack_email_secret_name = "civil-service-alert-slack-group-email"

monitor_action_group = {
  "demo-civil-service-slack-alert" = {
    short_name = "cvlsr-demo"
  }
}

scheduler_alerts_enabled = false
job_not_run_threshold    = 26

expected_schedulers = [
  "AutomatedHearingNotice",
  "BundleCreation",
  "CaseDismissed",
  "ClaimDetailsNotificationDeadline",
  "CoscApplicationProcessor",
  "DecisionOutcome",
  "FullAdmitPayImmediatelyNoPaymentFromDefendant",
  "GADocUploadNotifyScheduler",
  "GAOrderMadeScheduler",
  "GAUnlessOrderScheduler",
  "HearingCvpLink",
  "JudgementBuffer",
  "ManageStayWATask",
  "OrderReviewObligationCheck",
  "PollingEventEmitter",
  "RequestForReconsiderationNotification",
  "SettlementNoResponseFromDefendantCheck",
  "TakeCaseOffline",
  "TrialReadyCheck",
  "TrialReadyNotification"
]

monitor_cmc_db_metric_alerts = {
  "demo-cmc-db-cpu-warning" = {
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
    action = [{ action_group_name = "demo-civil-service-slack-alert" }]
  },
  "demo-cmc-db-cpu-critical" = {
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
    action = [{ action_group_name = "demo-civil-service-slack-alert" }]
  },
  "demo-cmc-db-storage-warning" = {
    description  = "Demo CMC DB disk usage >= 80%"
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
    action = [{ action_group_name = "demo-civil-service-slack-alert" }]
  },
  "demo-cmc-db-storage-critical" = {
    description  = "Demo CMC DB disk usage >= 90%"
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
    action = [{ action_group_name = "demo-civil-service-slack-alert" }]
  },
  "demo-cmc-db-memory-warning" = {
    description  = "Demo CMC DB memory usage >= 85%"
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
    action = [{ action_group_name = "demo-civil-service-slack-alert" }]
  },
  "demo-cmc-db-memory-critical" = {
    description  = "Demo CMC DB memory usage >= 95%"
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
    action = [{ action_group_name = "demo-civil-service-slack-alert" }]
  }
}
