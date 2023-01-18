
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

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "Vault ${data.azurerm_key_vault.send_grid.name}"
  })

  depends_on = [
    module.key-vault
  ]
}
