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
  default_action_group_key        = length(var.monitor_action_group) > 0 ? keys(var.monitor_action_group)[0] : null
  default_action_group_name       = local.default_action_group_key != null ? azurerm_monitor_action_group.civil-service-action-group[local.default_action_group_key].name : null

  expected_schedulers_kql = length(var.expected_schedulers) > 0 ? join(",\n        ", [for s in var.expected_schedulers : "\"${s}\""]) : "\"None\""
}

resource "azurerm_monitor_action_group" "civil-service-action-group" {
  for_each            = var.monitor_action_group
  name                = "${each.key}-${var.env}"
  resource_group_name = local.civil_service_rg_name
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

module "scheduler-fleet-aborted-alert" {
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location
  enabled  = tostring(var.scheduler_alerts_enabled)

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.civil_service_rg_name

  alert_name = "SchedulerFleetJobAbortedAlert-${var.env}"
  alert_desc = "Triggers when any scheduled task in ${var.env} has aborted."

  app_insights_query = <<-AIQ
      customEvents
        | where name == "SchedulerJobAborted"
        | extend schedulerName = tostring(customDimensions.schedulerName)
        | extend jobAbortReason = tostring(column_ifexists('customDimensions.jobAbortReason', 'Unknown'))
        | extend failedCases = toint(column_ifexists('customDimensions.failedCases', 0))
        | extend totalCases = toint(column_ifexists('customDimensions.totalCases', 0))
        | project timestamp, schedulerName, jobAbortReason, failedCases, totalCases
      AIQ

  custom_email_subject       = "Alert: A Civil Service scheduler has ABORTED in ${var.env}"
  frequency_in_minutes       = "15"
  time_window_in_minutes     = "30"
  severity_level             = "2"
  action_group_name          = local.default_action_group_name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = "0"
  common_tags                = var.common_tags
}

module "scheduler-fleet-high-failure-rate-alert" {
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location
  enabled  = tostring(var.scheduler_alerts_enabled)

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.civil_service_rg_name

  alert_name = "SchedulerFleetHighFailureRateAlert-${var.env}"
  alert_desc = "Triggers when any scheduled task in ${var.env} experiences a failure rate exceeding 20%."

  app_insights_query = <<-AIQ
      customEvents
        | where name in ("SchedulerJobCompleted", "SchedulerJobAborted")
        | extend schedulerName = tostring(customDimensions.schedulerName)
        | extend totalCases = toint(column_ifexists('customDimensions.totalCases', 0))
        | extend failedCases = toint(column_ifexists('customDimensions.failedCases', 0))
        | where totalCases > 0
        | extend failureRate = failedCases * 1.0 / totalCases
        | where failureRate > 0.2
        | project timestamp, schedulerName, totalCases, failedCases, failureRate
      AIQ

  custom_email_subject       = "Warning: A Civil Service scheduler has a HIGH FAILURE RATE in ${var.env}"
  frequency_in_minutes       = "30"
  time_window_in_minutes     = "60"
  severity_level             = "1"
  action_group_name          = local.default_action_group_name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = "0"
  common_tags                = var.common_tags
}

module "scheduler-fleet-job-not-run-alert" {
  source   = "git@github.com:hmcts/cnp-module-metric-alert"
  location = var.location
  enabled  = tostring(var.scheduler_alerts_enabled && length(var.expected_schedulers) > 0)

  app_insights_name  = module.application_insights.name
  resourcegroup_name = local.civil_service_rg_name

  alert_name = "SchedulerFleetJobNotRunAlert-${var.env}"
  alert_desc = "Triggers when any expected scheduled task in ${var.env} has not run within the last ${var.job_not_run_threshold} hours."

  app_insights_query = <<-AIQ
      let expectedSchedulers = datatable(expectedSchedulerName: string) [
        ${local.expected_schedulers_kql}
      ];
      let actualRuns = customEvents
        | where name == "SchedulerJobStarted"
        | where timestamp > ago(${var.job_not_run_threshold}h)
        | extend schedulerName = tostring(customDimensions.schedulerName)
        | summarize lastRun = max(timestamp) by schedulerName;
      expectedSchedulers
        | join kind=leftanti actualRuns on $left.expectedSchedulerName == $right.schedulerName
        | project missingScheduler = expectedSchedulerName
      AIQ

  custom_email_subject       = "Alert: An expected Civil Service scheduler has NOT RUN in ${var.env}"
  frequency_in_minutes       = "60"
  time_window_in_minutes     = "120"
  severity_level             = "2"
  action_group_name          = local.default_action_group_name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = "0"
  common_tags                = var.common_tags
}
