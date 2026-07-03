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
  "BundleCreation" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "HearingCvpLink" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "PollingEventEmitter" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "AutomatedHearingNotice" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "GenerateCsvAndSendToMmt" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "TakeCaseOffline" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "TrialReadyNotification" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "TrialReadyCheck" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "OrderReviewObligationCheck" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "DecisionOutcome" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "CaseDismissed" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "GADocUploadNotifyScheduler" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "CoscApplicationProcessor" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
  "GAOrderMadeScheduler" = {
    enabled      = false
    action_group = "demo-civil-service-slack-alert"
  }
}

job_not_run_threshold = 26
