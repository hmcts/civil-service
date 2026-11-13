variable "name" { type = string }
variable "resource_group_name" { type = string }
variable "location" { type = string }
variable "tags" { type = map(string) }
variable "config" {
  type = object({
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
  })
  validation {
    condition = alltrue([
      for report in [var.config] :
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
