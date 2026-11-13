variable "support_report_workflows" {
  description = "Opt-in telemetry workflows. Keep empty until platform, source and destination access is approved. Definitions are supplied by the report implementation tickets."
  type = map(object({
    enabled               = bool
    frequency             = string
    interval              = number
    start_time            = string
    time_zone             = string
    definition_json       = string
    log_resource_id       = string
    artifact_container_id = string
    ledger_container_id   = string
    action_group_id       = string
    deadline_hours        = number
  }))
  default = {}

  validation {
    condition = alltrue([
      for name, report in var.support_report_workflows :
      contains(["concurrency", "stuck-cases"], name) &&
      contains(["Day", "Week", "Month"], report.frequency) &&
      report.interval >= 1 && report.interval == floor(report.interval) &&
      report.deadline_hours >= 1 && report.deadline_hours <= 720 &&
      can(formatdate("YYYY", "${report.start_time}Z")) &&
      can(regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$", report.start_time)) &&
      try(length(jsondecode(report.definition_json).actions) > 0, false) &&
      can(regex("(?i)^/subscriptions/[^/]+/resourceGroups/[^/]+/providers/Microsoft.Insights/components/[^/]+$", report.log_resource_id)) &&
      can(regex("(?i)^/subscriptions/[^/]+/resourceGroups/[^/]+/providers/Microsoft.Storage/storageAccounts/[^/]+/blobServices/default/containers/[^/]+$", report.artifact_container_id)) &&
      can(regex("(?i)^/subscriptions/[^/]+/resourceGroups/[^/]+/providers/Microsoft.Storage/storageAccounts/[^/]+/blobServices/default/containers/[^/]+$", report.ledger_container_id)) &&
      can(regex("(?i)^/subscriptions/[^/]+/resourceGroups/[^/]+/providers/Microsoft.Insights/actionGroups/[^/]+$", report.action_group_id))
    ])
    error_message = "Use a supported report, valid schedule/start time, workflow definition, resource-scoped App Insights source, container-scoped storage and existing action group."
  }
}

module "support_report" {
  for_each = var.support_report_workflows
  source   = "./modules/support-report"

  name                = "${var.product}-${each.key}-report-${var.env}"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  tags                = var.common_tags
  config              = each.value
}

output "support_report_infrastructure" {
  description = "Report identities and resources for source-owner approval and report-specific integration. No credentials or callback URLs."
  value       = { for name, report in module.support_report : name => report.infrastructure }
}
