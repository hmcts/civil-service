provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "unspec-service-${var.env}"
  location = var.location

  tags = var.common_tags
}

module "key-vault" {
  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  name                    = "unspec-${var.env}"
  product                 = var.product
  env                     = var.env
  tenant_id               = var.tenant_id
  object_id               = var.jenkins_AAD_objectId
  resource_group_name     = azurerm_resource_group.rg.name
  product_group_object_id = "40c33f5a-24d0-4b22-a923-df8a80a59cd9"
  common_tags             = var.common_tags
  create_managed_identity = false
}

resource "azurerm_application_insights" "appinsights" {
  name                = "unspec-service-appinsights-${var.env}"
  location            = var.appinsights_location
  resource_group_name = azurerm_resource_group.rg.name
  application_type    = "web"
}
