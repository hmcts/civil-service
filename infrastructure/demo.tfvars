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
  "FullAdmitPayImmediatelyNoPaymentFromDefendant" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
}

job_not_run_threshold = 26
