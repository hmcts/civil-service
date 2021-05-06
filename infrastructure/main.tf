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

locals {
  vaultName = "${var.product}-${var.env}"
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-${var.env}"
  location            = var.appinsights_location
  resource_group_name = azurerm_resource_group.rg.name
  application_type    = "web"
}
