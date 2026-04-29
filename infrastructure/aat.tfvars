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
    threshold                         = 0
    severity                          = 2
    evaluation_frequency              = "PT1H"
    window_duration                   = "PT1H"
    auto_mitigation_enabled           = true
    workspace_alerts_storage_enabled  = false
    enabled                           = true
    skip_query_validation             = true
  }
  "DefendantResponseDeadline" = {
    threshold                         = 0
    severity                          = 2
    evaluation_frequency              = "PT1H"
    window_duration                   = "PT1H"
    auto_mitigation_enabled           = true
    workspace_alerts_storage_enabled  = false
    enabled                           = true
    skip_query_validation             = true
  }
}
