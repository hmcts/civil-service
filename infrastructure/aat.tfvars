#================================================================================================
# Azure Monitor
#================================================================================================
civil_service_alert_slack_email_secret_name = "civil-service-alert-slack-group-email"

monitor_action_group = {
  "aat-civil-service-slack-alert" = {
    short_name = "cvlsr-aat"
  }
}

monitor_scheduler_alerts = {
  "JudgementBuffer" = {
    frequency_in_minutes              = 60
    time_window_in_minutes            = 60
    enabled                           = true
  }
  "DefendantResponseDeadline" = {
    frequency_in_minutes              = 60
    time_window_in_minutes            = 60
    enabled                           = true
  }
}
