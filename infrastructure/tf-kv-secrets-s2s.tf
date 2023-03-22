data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
  name         = "microservicekey-civil-service"
}

resource "azurerm_key_vault_secret" "civil_s2s_secret" {
  name         = "microservicekey-civil-service"
  value        = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = module.key-vault.key_vault_id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "Vault ${data.azurerm_key_vault.s2s_vault.name}"
  })

  depends_on = [
    module.key-vault
  ]
}