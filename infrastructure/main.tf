provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location

  tags = var.common_tags
}

module "key-vault" {
  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  name                    = "civil-${var.env}"
  product                 = var.product
  env                     = var.env
  tenant_id               = var.tenant_id
  object_id               = var.jenkins_AAD_objectId
  resource_group_name     = azurerm_resource_group.rg.name
  product_group_object_id = "ca5067a5-f554-4f6a-9eda-e93a1190d7ec"
  common_tags             = var.common_tags
  create_managed_identity = true
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-${var.env}"
  location            = var.appinsights_location
  resource_group_name = azurerm_resource_group.rg.name
  application_type    = "web"
}

data "azurerm_key_vault" "send_grid" {
  provider = azurerm.send-grid

  name                = var.env != "prod" ? "sendgridnonprod" : "sendgridprod"
  resource_group_name = var.env != "prod" ? "SendGrid-nonprod" : "SendGrid-prod"
}

data "azurerm_key_vault_secret" "send_grid_api_key" {
  provider = azurerm.send-grid

  key_vault_id = data.azurerm_key_vault.send_grid.id
  name         = "hmcts-civil-api-key"
}

resource "azurerm_key_vault_secret" "sendgrid_api_key" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "sendgrid-api-key"
  value        = data.azurerm_key_vault_secret.send_grid_api_key.value

  depends_on = [
    module.key-vault
  ]
}

data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = "${data.azurerm_key_vault.s2s_vault.id}"
  name = "microservicekey-civil-service"
}

resource "azurerm_key_vault_secret" "civil_s2s_secret" {
  name         = "microservicekey-civil-service"
  value        = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = module.key-vault.key_vault_id

  depends_on = [
    module.key-vault
  ]
}

data "azurerm_key_vault" "cmc_vault" {
  name                = "cmc-${var.env}"
  resource_group_name = "cmc-${var.env}"
}

data "azurerm_key_vault_secret" "db_password_secret" {
  key_vault_id = "${data.azurerm_key_vault.cmc_vault.id}"
  name = "cmc-db-password"
}

resource "azurerm_key_vault_secret" "civil_db_password_secret" {
  name         = "cmc-db-password"
  value        = data.azurerm_key_vault_secret.db_password_secret.value
  key_vault_id = module.key-vault.key_vault_id

  depends_on = [
    module.key-vault
  ]
}
