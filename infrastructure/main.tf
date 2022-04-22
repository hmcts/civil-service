provider "azurerm" {
  features {}
}

locals {
  common_tags = module.ctags.common_tags
}

module "ctags" {
  source      = "git::https://github.com/hmcts/terraform-module-common-tags.git?ref=master"
  environment = var.environment
  product     = var.product
  builtFrom   = var.builtFrom
} 

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location

  tags = local.common_tags
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
  common_tags             = local.common_tags
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

data "azurerm_key_vault_secret" "db_password_v11_secret" {
  key_vault_id = "${data.azurerm_key_vault.cmc_vault.id}"
  name = "cmc-db-password-v11"
}

resource "azurerm_key_vault_secret" "civil_db_password__v11_secret" {
  name         = "cmc-db-password-v11"
  value        = data.azurerm_key_vault_secret.db_password_v11_secret.value
  key_vault_id = module.key-vault.key_vault_id

  depends_on = [
    module.key-vault
  ]
}

data "azurerm_key_vault" "ethos_vault" {
  name                = "ethos-${var.env}"
  resource_group_name = "ethos-repl-docmosis-backend-${var.env}"
}

data "azurerm_key_vault_secret" "tornado_access_secret" {
  key_vault_id = "${data.azurerm_key_vault.ethos_vault.id}"
  name = "tornado-access-key"
}

resource "azurerm_key_vault_secret" "civil_docmosis_api_key" {
  name         = "docmosis-api-key"
  value        = data.azurerm_key_vault_secret.tornado_access_secret.value
  key_vault_id = module.key-vault.key_vault_id

  depends_on = [
    module.key-vault
  ]
}
