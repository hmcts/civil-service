send_grid_subscription = "8999dec3-0104-4a27-94ee-6588559729d1"

custom_alerts_enabled = 1

#================================================================================================
# Azure Monitor - service-health alerts (DTSCCI-5823 / MIR 5, incident 179051)
#
# TODO before enabling in prod: prod has no action group or Slack email secret configured yet.
# To activate the CCD callback 502-rate alert (Signal 2), set the three blocks below with the real
# prod values and flip enabled = true. Confirm the Slack channel is watched out-of-hours (MIR 6).
#
# civil_service_alert_slack_email_secret_name = "civil-service-alert-slack-group-email" # must exist in the prod Key Vault
#
# monitor_action_group = {
#   "prod-civil-service-slack-alert" = {
#     short_name = "cvlsr-prod"
#   }
# }
#
# monitor_service_health_alerts = {
#   "CcdCallback502Rate" = {
#     enabled                 = true
#     action_group            = "prod-civil-service-slack-alert"
#     failure_count_threshold = 40
#     frequency_in_minutes    = 5
#     time_window_in_minutes  = 30
#     severity_level          = "1"
#   }
# }
#================================================================================================

ccd_service_bus_status = "Disabled"

ccd_service_bus_filter_rule = "jurisdiction_id IN ('IMPOSSIBLE_VALUE')"
