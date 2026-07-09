# Service-health alerts (DTSCCI-5823 / MIR 5, P1 incident 179051).
#
# Signal 2 (civil-owned): sustained rate of CCD callback 502 wrappers that civil-service logs
# when CCD wraps an S2S 403 rejection. A rising rate is an early indicator of the secret-mount /
# S2S degradation behind the 30 Jun P1. Back-tested against 29-30 Jun: fires ~16:00 on 29 Jun,
# the evening before the incident was raised (see DTSCCI-5823 for the evidence and threshold).
#
# Map-driven and disabled by default, mirroring monitor_scheduler_alerts. Reuses the existing
# civil-service-action-group (defined in scheduler-alerts.tf) for notification.
#
# NOTE: query runs against civil-service's own App Insights resource, so it uses the classic AI
# schema (exceptions / cloud_RoleName / problemId), not the Log Analytics App* schema.

module "callback-failure-rate-alerts" {
  for_each = var.monitor_service_health_alerts
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location
  enabled  = tostring(try(each.value.enabled, false))

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.resource_group_name

  alert_name = "${each.key}-${var.env}"
  alert_desc = "Triggers when civil-service logs a sustained rate of CCD callback 502s (S2S rejection precursor) in ${var.env}. See DTSCCI-5823 / incident 179051."

  app_insights_query = <<-AIQ
      exceptions
        | where cloud_RoleName == "HMCTS Civil Service"
        | where problemId has "feign.FeignException.BadGateway"
        | summarize badGateway502 = count()
        | where badGateway502 > ${try(each.value.failure_count_threshold, 40)}
      AIQ

  custom_email_subject       = "Warning: civil-service CCD callback 502 rate elevated in ${var.env} (possible S2S / secret-mount degradation)"
  frequency_in_minutes       = tostring(try(each.value.frequency_in_minutes, 5))
  time_window_in_minutes     = tostring(try(each.value.time_window_in_minutes, 30))
  severity_level             = try(each.value.severity_level, "1")
  action_group_name          = azurerm_monitor_action_group.civil-service-action-group[each.value.action_group].name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = "0"
  common_tags                = var.common_tags
}
