data "azurerm_key_vault" "ethos_vault" {
  name                = "ethos-${var.env}"
  resource_group_name = "ethos-repl-docmosis-backend-${var.env}"
}

data "azurerm_key_vault_secret" "tornado_access_secret" {
  key_vault_id = data.azurerm_key_vault.ethos_vault.id
  name         = "tornado-access-key"
}

resource "azurerm_key_vault_secret" "civil_docmosis_api_key" {
  name         = "docmosis-api-key"
  value        = data.azurerm_key_vault_secret.tornado_access_secret.value
  key_vault_id = module.key-vault.key_vault_id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "Vault ${data.azurerm_key_vault.ethos_vault.name}"
  })

  depends_on = [
    module.key-vault
  ]
}