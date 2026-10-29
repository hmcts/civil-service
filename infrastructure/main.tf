
resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location

  tags = var.common_tags
}

data "azurerm_user_assigned_identity" "app_mi" {
  name                = "${var.product}-${var.env}-mi"
  resource_group_name = "managed-identities-${var.env}-rg"

  depends_on = [
    module.key-vault
  ]
}

data "azurerm_key_vault" "civil_key_vault" {
  name                = "civil-${var.env}"
  resource_group_name = "civil-service-${var.env}"
}

data "azurerm_key_vault" "ccd_key_vault" {
  name                = "ccd-${var.env}"
  resource_group_name = "ccd-shared-${var.env}"
}

//Retrieve and copy secret from ccd vault into civil
data "azurerm_key_vault_secret" "ccd_shared_servicebus_secret" {
  key_vault_id = data.azurerm_key_vault.ccd_key_vault.id
  name         = "ccd-servicebus-connection-string"
}

resource "azurerm_key_vault_secret" "ccd_shared_servicebus_connection_string" {
  name         = "ccd-shared-servicebus-connection-string"
  value        = data.azurerm_key_vault_secret.ccd_shared_servicebus_secret.value
  key_vault_id = data.azurerm_key_vault.civil_key_vault.id
}

locals {
  # Retrieves the Slack email address from Key Vault if the secret exists, otherwise defaults to null
  civil_service_alert_slack_email = length(data.azurerm_key_vault_secret.civil-service-alert-slack-email) > 0 ? data.azurerm_key_vault_secret.civil-service-alert-slack-email[0].value : null
  civil_service_rg_name           = "civil-service-${var.env}"
  default_action_group_key        = length(var.monitor_action_group) > 0 ? keys(var.monitor_action_group)[0] : null
  default_action_group_name       = local.default_action_group_key != null ? azurerm_monitor_action_group.civil-service-action-group[local.default_action_group_key].name : null

  expected_schedulers_kql = length(var.expected_schedulers) > 0 ? join(",\n        ", [for s in var.expected_schedulers : "\"${s}\""]) : "\"None\""
  cmc_db_rg_name          = "cmc-db-v15-data-${var.env}"
}
