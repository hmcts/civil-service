data "azurerm_key_vault_secret" "civil-service-alert-slack-email" {
  # Conditionally create the data source only if the secret name is provided
  count        = var.civil_service_alert_slack_email_secret_name != null ? 1 : 0
  name         = var.civil_service_alert_slack_email_secret_name
  key_vault_id = module.key-vault.key_vault_id
}

locals {
  # Retrieves the Slack email address from Key Vault if the secret exists, otherwise defaults to null
  civil_service_alert_slack_email = length(data.azurerm_key_vault_secret.civil-service-alert-slack-email) > 0 ? data.azurerm_key_vault_secret.civil-service-alert-slack-email[0].value : null
  resource_group_name             = "civil-service-${var.env}"
}

resource "azurerm_monitor_action_group" "civil-service-action-group" {
  for_each            = var.monitor_action_group
  name                = "${each.key}-${var.env}"
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
  for_each = var.monitor_scheduler_alerts
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location
  enabled  = tostring(try(each.value.enabled, false))

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.resource_group_name

  alert_name = "${each.key}JobAbortedAlert-${var.env}"
  alert_desc = "Triggers when scheduler ${each.key} in ${var.env} has aborted."

  app_insights_query = <<-AIQ
      customEvents
        | where name == "${each.key}JobAborted"
        | project timestamp, name, properties.abortReason
      AIQ

  custom_email_subject       = "Warning: The scheduler ${each.key} in ${var.env} has aborted."
  frequency_in_minutes       = tostring(try(each.value.frequency_in_minutes, 30))
  time_window_in_minutes     = tostring(try(each.value.time_window_in_minutes, 60))
  severity_level             = "2"
  action_group_name          = azurerm_monitor_action_group.civil-service-action-group[each.value.action_group].name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = "0"
  common_tags                = var.common_tags
}

module "scheduler-high-failure-rate-alerts" {
  for_each = var.monitor_scheduler_alerts
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location
  enabled  = tostring(try(each.value.enabled, false))

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.resource_group_name

  alert_name = "${each.key}HighFailureRateAlert-${var.env}"
  alert_desc = "Triggers when scheduler ${each.key} in ${var.env} has a high failure rate."

  app_insights_query = <<-AIQ
      customEvents
        | where name in ("${each.key}JobCompleted", "${each.key}JobAborted")
        | extend cases = toint(column_ifexists('properties.totalCases', 0))
        | extend failed = toint(column_ifexists('properties.failedCases', 0))
        | where cases > 0
        | extend failureRate = failed * 1.0 / cases
        | project timestamp, name, cases, failed, failureRate
        | where failureRate > 0.2
      AIQ

  custom_email_subject       = "Warning: The scheduler ${each.key} in ${var.env} has a high failure rate."
  frequency_in_minutes       = tostring(try(each.value.frequency_in_minutes, 30))
  time_window_in_minutes     = tostring(try(each.value.time_window_in_minutes, 60))
  severity_level             = "1"
  action_group_name          = azurerm_monitor_action_group.civil-service-action-group[each.value.action_group].name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = "0"
  common_tags                = var.common_tags
}

module "scheduler-job-not-run-alerts" {
  for_each = var.monitor_scheduler_alerts
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location
  enabled  = tostring(try(each.value.enabled, false))

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.resource_group_name

  alert_name = "${each.key}JobNotRunAlert-${var.env}"
  alert_desc = "Triggers when scheduler ${each.key} in ${var.env} has not run in the last ${var.job_not_run_threshold} hours."

  app_insights_query = <<-AIQ
      customEvents
        | where name == "${each.key}JobStarted"
        | where timestamp > ago(${var.job_not_run_threshold}h)
      AIQ

  custom_email_subject       = "Warning: The scheduler ${each.key} in ${var.env} has not run in the last ${var.job_not_run_threshold} hours."
  frequency_in_minutes       = tostring(try(each.value.frequency_in_minutes, 30))
  time_window_in_minutes     = tostring(try(each.value.time_window_in_minutes, 60))
  severity_level             = "2"
  action_group_name          = azurerm_monitor_action_group.civil-service-action-group[each.value.action_group].name
  trigger_threshold_operator = "LessThan"
  trigger_threshold          = "1"
  common_tags                = var.common_tags
}
