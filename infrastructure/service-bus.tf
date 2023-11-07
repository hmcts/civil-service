#HMC to Hearings API

locals {
  sql_filters = {
    "hmc-servicebus-${var.env}-subscription-rule-civil" : {
      sql_filter = "hmctsServiceId IN ('AAA7','AAA6')"
    }
  }

  correlation_filters = {
    correlation_filter1 : {
      properties = {
        hmctsProperty = "any"
      }
    }
  }
}

module "servicebus-subscription" {
  source              = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=DTSPO-15347-add-subscription_rule"
  name                = "hmc-to-civil-subscription-${var.env}"
  namespace_name      = "hmc-servicebus-${var.env}"
  topic_name          = "hmc-to-cft-${var.env}"
  resource_group_name = "hmc-shared-${var.env}"
  sql_filters         = local.sql_filters
  correlation_filters = local.correlation_filters
}

data "azurerm_key_vault" "hmc-key-vault" {
  name                = "hmc-${var.env}"
  resource_group_name = "hmc-shared-${var.env}"
}

data "azurerm_key_vault_secret" "hmc-servicebus-connection-string" {
  key_vault_id = data.azurerm_key_vault.hmc-key-vault.id
  name         = "hmc-servicebus-connection-string"
}

resource "azurerm_key_vault_secret" "hmc-servicebus-connection-string" {
  name         = "hmc-servicebus-connection-string"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-connection-string.value
  key_vault_id = data.azurerm_key_vault.civil_key_vault.id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "Vault ${data.azurerm_key_vault.civil_key_vault.name}"
  })
}


data "azurerm_key_vault_secret" "hmc-servicebus-shared-access-key" {
  key_vault_id = data.azurerm_key_vault.hmc-key-vault.id
  name         = "hmc-servicebus-shared-access-key"
}

resource "azurerm_key_vault_secret" "civil-hmc-servicebus-shared-access-key-tf" {
  name         = "hmc-servicebus-shared-access-key-tf"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-shared-access-key.value
  key_vault_id = data.azurerm_key_vault.civil_key_vault.id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "Vault ${data.azurerm_key_vault.civil_key_vault.name}"
  })
}
