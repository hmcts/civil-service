
locals {
  send_grid_key_vault_name                = var.env != "prod" ? "sendgridnonprod" : "sendgridprod"
  send_grid_key_vault_resource_group_name = var.env != "prod" ? "SendGrid-nonprod" : "SendGrid-prod"
  send_grid_key_vault_id                  = "/subscriptions/${var.send_grid_subscription}/resourceGroups/${local.send_grid_key_vault_resource_group_name}/providers/Microsoft.KeyVault/vaults/${local.send_grid_key_vault_name}"
}

data "azurerm_key_vault_secret" "send_grid_api_key" {
  provider = azurerm.send-grid

  key_vault_id = local.send_grid_key_vault_id
  name         = "hmcts-civil-api-key"
}

resource "azurerm_key_vault_secret" "sendgrid_api_key" {
  key_vault_id = module.key-vault.key_vault_id
  name         = "sendgrid-api-key"
  value        = data.azurerm_key_vault_secret.send_grid_api_key.value

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "Vault ${local.send_grid_key_vault_name}"
  })

  depends_on = [
    module.key-vault
  ]
}
