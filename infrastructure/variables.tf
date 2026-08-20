variable "product" {}

variable "component" {}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "subscription" {}

variable "common_tags" {
  type = map(string)
}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environment variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "appinsights_location" {
  type        = string
  default     = "UK South"
  description = "Location for Application Insights"
}

variable "send_grid_subscription" {
  default = "1c4f0704-a29e-403d-b719-b90c34ef14c9"
}

variable "custom_alerts_enabled" {
  default = 0
}

variable "ccd_service_bus_status" {
  type    = string
  default = "Active"
}

variable "ccd_service_bus_filter_rule" {
  type        = string
  default     = "jurisdiction_id IN ('CIVIL','civil')"
  description = "SQL filter rule for CCD Events Service Bus Subscription"
}

#================================================================================================
# Monitor Variables
#================================================================================================
variable "monitor_action_group" {
  type = map(object({
    short_name = optional(string)
  }))
  default     = {}
  description = "Map of monitor action groups to create"
}

variable "monitor_scheduler_alerts" {
  type = map(object({
    frequency_in_minutes   = optional(number)
    time_window_in_minutes = optional(number)
    enabled                = optional(bool)
    action_group           = string
  }))
  default     = {}
  description = "Map of scheduler alerts to create, with action_group mapping"
}

variable "civil_service_alert_slack_email_secret_name" {
  type        = string
  description = "The name of the Key Vault secret containing the slack email group"
  default     = null
}

variable "job_not_run_threshold" {
  type        = number
  description = "The threshold in hours for the scheduler job not run alert"
  default     = 26
}

