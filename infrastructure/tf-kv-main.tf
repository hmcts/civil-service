data "azuread_group" "dts_civil" {
  display_name     = "DTS Civil"
  security_enabled = true
}

data "azurerm_user_assigned_identity" "jenkins" {
  name                = "jenkins-${var.env}-mi"
  resource_group_name = "managed-identities-${var.env}-rg"
}

data "azurerm_user_assigned_identity" "jenkins-preview" {
  provider = azurerm.cnp_dev
  count    = var.env == "aat" ? 1 : 0

  # Temporary exception for DTSPO-30107: Civil preview deploys currently read
  # AAT team secrets because the Jenkins library maps preview vaults to AAT.
  # Remove once preview secret loading no longer requires AAT vault access.
  name                = "jenkins-preview-mi"
  resource_group_name = "managed-identities-preview-rg"
}

module "key-vault" {
  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=DTSPO-31965/remove-jenkins-ptl-access"
  name                    = "${var.product}-${var.env}"
  product                 = var.product
  env                     = var.env
  tenant_id               = var.tenant_id
  object_id               = var.jenkins_AAD_objectId
  resource_group_name     = azurerm_resource_group.rg.name
  product_group_object_id = data.azuread_group.dts_civil.object_id
  jenkins_object_id       = data.azurerm_user_assigned_identity.jenkins.principal_id
  common_tags             = var.common_tags
  create_managed_identity = true
  managed_identity_object_ids = var.env == "aat" ? [
    data.azurerm_user_assigned_identity.jenkins-preview[0].principal_id
  ] : []
}
