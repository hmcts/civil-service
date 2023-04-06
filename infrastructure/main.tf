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
