#HMC to Hearings API
module "servicebus-subscription" {
  source              = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=master"
  name                = "hmc-to-civil-subscription-${var.env}"
  namespace_name      = "hmc-servicebus-${var.env}"
  topic_name          = "hmc-to-cft-${var.env}"
  resource_group_name = "hmc-shared-${var.env}"
}

resource "azurerm_servicebus_subscription_rule" "topic_filter_rule_civil" {
  name            = "hmc-servicebus-${var.env}-subscription-rule-civil"
  subscription_id = module.servicebus-subscription.id
  filter_type     = "CorrelationFilter"

  correlation_filter {
    properties = {
      hmctsServiceId = "AAA7"
    }
  }
}

data "azurerm_key_vault" "hmc-key-vault" {
  name                = "hmc-${var.env}"
  resource_group_name = "hmc-shared-${var.env}"
}

data "azurerm_key_vault_secret" "hmc-servicebus-connection-string" {
  key_vault_id = data.azurerm_key_vault.hmc-key-vault.id
  name         = "hmc-servicebus-connection-string"
}

