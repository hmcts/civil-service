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
    enabled      = true
    action_group = "demo-civil-service-slack-alert"
  }
}
