data "azurerm_key_vault_secret" "civil_service_alert_slack_email" {
  count        = var.civil_service_alert_slack_email_secret_name != null ? 1 : 0
  name         = var.civil_service_alert_slack_email_secret_name
  key_vault_id = module.key-vault.key_vault_id
}

locals {
  civil_service_alert_slack_email = length(data.azurerm_key_vault_secret.civil_service_alert_slack_email) > 0 ? data.azurerm_key_vault_secret.civil_service_alert_slack_email[0].value : null
}

resource "azurerm_monitor_action_group" "civil_service_action_group" {
  for_each            = var.monitor_action_group
  name                = each.key
  resource_group_name = azurerm_resource_group.rg.name
  short_name          = try(each.value.short_name, null)
  tags                = var.common_tags

  dynamic "email_receiver" {
    for_each = local.civil_service_alert_slack_email != null ? [1] : []
    content {
      name                    = "slack-email"
      email_address           = local.civil_service_alert_slack_email
      use_common_alert_schema = true
    }
  }
}

module "stuck_cases_alerts" {
  for_each = var.monitor_stuck_cases_alerts
  source   = "git@github.com:hmcts/cnp-module-metric-alert"

  location           = var.location
  app_insights_name  = module.application_insights.name
  resourcegroup_name = azurerm_resource_group.rg.name

  alert_name                 = each.key
  alert_desc                 = "Triggers when stuck cases requiring manual intervention are detected in ${var.env}."
  app_insights_query         = <<-AIQ
      customEvents
      | where name == "StuckCasesDailyDigest"
      | where tostring(customDimensions.manualInterventionRequired) == "true"
      | extend stuckCaseCount = toint(tostring(customDimensions.stuckCaseCount))
      | extend caseIds = tostring(customDimensions.caseIds)
      | extend incidentStartTime = tostring(customDimensions.incidentStartTime)
      | extend incidentEndTime = tostring(customDimensions.incidentEndTime)
      | project timestamp, name, stuckCaseCount, caseIds, incidentStartTime, incidentEndTime
    AIQ
  custom_email_subject       = "Warning: Stuck cases requiring manual intervention detected in ${var.env}"
  frequency_in_minutes       = try(each.value.frequency_in_minutes, 1440)
  time_window_in_minutes     = try(each.value.time_window_in_minutes, 1440)
  severity_level             = try(each.value.severity_level, 3)
  action_group_name          = values(azurerm_monitor_action_group.civil_service_action_group)[0].name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = 0
  common_tags                = var.common_tags
}
