#
# Azure Monitor
#
civil_service_alert_slack_email_secret_name = "civil-alerting-test-slack-group-email"

monitor_action_group = {
  "demo-civil-alerting-test-slack-alert" = {
    short_name = "cvalt-demo"
  }
}

monitor_stuck_cases_alerts = {
  "StuckCasesDailyDigest" = {
    frequency_in_minutes   = 60
    time_window_in_minutes = 60
    severity_level         = 3
  }
}
