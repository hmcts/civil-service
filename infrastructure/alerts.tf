data "azurerm_key_vault_secret" "civil-service-alert-slack-email" {
  count        = var.civil_service_alert_slack_email_secret_name != null ? 1 : 0
  name         = var.civil_service_alert_slack_email_secret_name
  key_vault_id = module.key-vault.key_vault_id
}

locals {
  alerts_resource_group_name = "${var.product}-${var.component}-alerts-${var.env}"
  civil_service_alert_slack_email      = length(data.azurerm_key_vault_secret.civil-service-alert-slack-email) > 0 ? data.azurerm_key_vault_secret.civil-service-alert-slack-email[0].value : null
}

resource "azurerm_monitor_action_group" "civil-service-action-group" {
  for_each            = var.monitor_action_group
  name                = each.key
  resource_group_name = local.alerts_resource_group_name
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

resource "azurerm_monitor_scheduled_query_rules_alert_v2" "scheduler_aborted_alerts" {
  for_each                          = var.monitor_scheduler_alerts

  name                              = "${each.key}JobAborted"
  display_name                      = "${each.key}JobAborted"
  description                       = "The scheduler ${each.key} in ${var.env} has aborted."

  resource_group_name               = local.alerts_resource_group_name
  location                          = var.location
  evaluation_frequency              = try(each.value.evaluation_frequency, null)
  window_duration                   = try(each.value.window_duration, null)
  scopes                            = [module.application_insights.id]
  severity                          = try(each.value.severity, null)
  auto_mitigation_enabled           = try(each.value.auto_mitigation_enabled, null)
  workspace_alerts_storage_enabled  = try(each.value.workspace_alerts_storage_enabled, null)
  enabled                           = try(each.value.enabled, null)
  skip_query_validation             = try(each.value.skip_query_validation, null)
  tags                              = var.common_tags

  criteria {
    query                   = <<-QUERY
      customEvents
        | where name == "${each.key}JobAborted"
        | project timestamp, name, properties.abortReason
      QUERY
    time_aggregation_method = "Count"
    threshold               = try(each.value.threshold, null)
    operator                = "GreaterThan"

    failing_periods {
      minimum_failing_periods_to_trigger_alert = 1
      number_of_evaluation_periods             = 1
    }
  }

  action {
    action_groups = [azurerm_monitor_action_group.civil-service-action-group["aat-civil-service-slack-alert"].id]
  }
}

resource "azurerm_monitor_scheduled_query_rules_alert_v2" "scheduler_high_failure_rate_alerts" {
  for_each                          = var.monitor_scheduler_alerts

  name                              = "${each.key}HighFailureRate"
  display_name                      = "${each.key}HighFailureRate"
  description                       = "The scheduler ${each.key} in ${var.env} has a high failure rate."

  resource_group_name               = local.alerts_resource_group_name
  location                          = var.location
  evaluation_frequency              = try(each.value.evaluation_frequency, null)
  window_duration                   = try(each.value.window_duration, null)
  scopes                            = [module.application_insights.id]
  severity                          = try(each.value.severity, null)
  auto_mitigation_enabled           = try(each.value.auto_mitigation_enabled, null)
  workspace_alerts_storage_enabled  = try(each.value.workspace_alerts_storage_enabled, null)
  enabled                           = try(each.value.enabled, null)
  skip_query_validation             = try(each.value.skip_query_validation, null)
  tags                              = var.common_tags

  criteria {
    query                   = <<-QUERY
      customEvents
        | where name in ("${each.key}JobCompleted", "${each.key}JobAborted")
        | extend cases = toint(tostring(properties.totalCases))
        | extend failed = toint(tostring(properties.failedCases))
        | extend failureRate = failed * 1.0 / cases
        | where failureRate > 0.2
      QUERY
    time_aggregation_method = "Count"
    threshold = try(each.value.threshold, null)
    operator                = "GreaterThan"

    failing_periods {
      minimum_failing_periods_to_trigger_alert = 1
      number_of_evaluation_periods             = 1
    }
  }
}

resource "azurerm_monitor_scheduled_query_rules_alert_v2" "scheduler_job_not_run_alerts" {
  for_each                          = var.monitor_scheduler_alerts

  name                              = "${each.key}JobNotRun"
  display_name                      = "${each.key}JobNotRun"
  description                       = "The scheduler ${each.key} in ${var.env} has not run in the last 26 hours."

  resource_group_name               = local.alerts_resource_group_name
  location                          = var.location
  evaluation_frequency              = try(each.value.evaluation_frequency, null)
  window_duration                   = try(each.value.window_duration, null)
  scopes                            = [module.application_insights.id]
  severity                          = try(each.value.severity, null)
  auto_mitigation_enabled           = try(each.value.auto_mitigation_enabled, null)
  workspace_alerts_storage_enabled  = try(each.value.workspace_alerts_storage_enabled, null)
  enabled                           = try(each.value.enabled, null)
  skip_query_validation             = try(each.value.skip_query_validation, null)
  tags                              = var.common_tags

  criteria {
    query                   = <<-QUERY
      customEvents
        | where name == "${each.key}JobStarted"
        | where timestamp > ago(26h)
      QUERY
    time_aggregation_method = "Count"
    threshold               = try(each.value.threshold, null)
    operator                = "GreaterThan"

    failing_periods {
      minimum_failing_periods_to_trigger_alert = 1
      number_of_evaluation_periods             = 1
    }
  }

  action {
    action_groups = [azurerm_monitor_action_group.civil-service-action-group["aat-civil-service-slack-alert"].id]
  }
}
