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
