data "azurerm_key_vault_secret" "civil-service-alert-slack-email" {
  count        = var.civil_service_alert_slack_email_secret_name != null ? 1 : 0
  name         = var.civil_service_alert_slack_email_secret_name
  key_vault_id = module.key-vault.key_vault_id
}

locals {
  civil_service_alert_slack_email = length(data.azurerm_key_vault_secret.civil-service-alert-slack-email) > 0 ? data.azurerm_key_vault_secret.civil-service-alert-slack-email[0].value : null
  enabled_scheduler_alerts        = { for k, v in var.monitor_scheduler_alerts : k => v if try(v.enabled, true) }
  resource_group_name             = "civil-service-${var.env}"
}

resource "azurerm_monitor_action_group" "civil-service-action-group" {
  for_each            = var.monitor_action_group
  name                = each.key
  resource_group_name = local.resource_group_name
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

module "scheduler-aborted-alerts" {
  for_each = local.enabled_scheduler_alerts
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.resource_group_name

  alert_name = "${each.key}JobAborted"
  alert_desc = "Triggers when scheduler ${each.key} in ${var.env} has aborted."

  app_insights_query = <<-AIQ
      customEvents
        | where name == "${each.key}JobAborted"
        | project timestamp, name, properties.abortReason
      AIQ

  custom_email_subject       = "Warning: The scheduler ${each.key} in ${var.env} has aborted."
  frequency_in_minutes       = try(each.value.frequency_in_minutes, 30)
  time_window_in_minutes     = try(each.value.time_window_in_minutes, 30)
  severity_level             = 3
  action_group_name          = azurerm_monitor_action_group.civil-service-action-group[each.value.action_group].name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = 0
  common_tags                = var.common_tags
}

module "scheduler-high-failure-rate-alerts" {
  for_each = local.enabled_scheduler_alerts
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.resource_group_name

  alert_name = "${each.key}HighFailureRate"
  alert_desc = "Triggers when scheduler ${each.key} in ${var.env} has a high failure rate."

  app_insights_query = <<-AIQ
      customEvents
        | where name in ("${each.key}JobCompleted", "${each.key}JobAborted")
        | extend cases = toint(tostring(properties.totalCases))
        | extend failed = toint(tostring(properties.failedCases))
        | extend failureRate = failed * 1.0 / cases
        | where failureRate > 0.2
      AIQ

  custom_email_subject       = "Warning: The scheduler ${each.key} in ${var.env} has a high failure rate."
  frequency_in_minutes       = try(each.value.frequency_in_minutes, 30)
  time_window_in_minutes     = try(each.value.time_window_in_minutes, 30)
  severity_level             = 3
  action_group_name          = azurerm_monitor_action_group.civil-service-action-group[each.value.action_group].name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = 0
  common_tags                = var.common_tags
}

module "scheduler-job-not-run-alerts" {
  for_each = local.enabled_scheduler_alerts
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.resource_group_name

  alert_name = "${each.key}JobNotRun"
  alert_desc = "Triggers when scheduler ${each.key} in ${var.env} has not run in the last 26 hours."

  app_insights_query = <<-AIQ
      customEvents
        | where name == "${each.key}JobStarted"
        | where timestamp > ago(26h)
      AIQ

  custom_email_subject       = "Warning: The scheduler ${each.key} in ${var.env} has not run in the last 26 hours."
  frequency_in_minutes       = try(each.value.frequency_in_minutes, 30)
  time_window_in_minutes     = try(each.value.time_window_in_minutes, 30)
  severity_level             = 3
  action_group_name          = azurerm_monitor_action_group.civil-service-action-group[each.value.action_group].name
  trigger_threshold_operator = "LessThan"
  trigger_threshold          = 1
  common_tags                = var.common_tags
}
