data "azuread_group" "dts_civil" {
  display_name     = "DTS Civil"
  security_enabled = true
}

module "key-vault" {
  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  name                    = "${var.product}-${var.env}"
  product                 = var.product
  env                     = var.env
  tenant_id               = var.tenant_id
  object_id               = var.jenkins_AAD_objectId
  resource_group_name     = azurerm_resource_group.rg.name
  product_group_object_id = data.azuread_group.dts_civil.object_id
  common_tags             = var.common_tags
  create_managed_identity = true
}