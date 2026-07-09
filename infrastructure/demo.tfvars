#================================================================================================
# Azure Monitor
#================================================================================================
civil_service_alert_slack_email_secret_name = "civil-service-alert-slack-group-email"

monitor_action_group = {
  "demo-civil-service-slack-alert" = {
    short_name = "cvlsr-demo"
  }
}

monitor_scheduler_alerts = {
  "JudgementBuffer" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
}

monitor_service_health_alerts = {
  "CcdCallback502Rate" = {
    enabled                 = false
    action_group            = "demo-civil-service-slack-alert"
    failure_count_threshold = 40
    frequency_in_minutes    = 5
    time_window_in_minutes  = 30
    severity_level          = "1"
  }
}

job_not_run_threshold = 26
