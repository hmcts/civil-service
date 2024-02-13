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
  default     = 0
}
