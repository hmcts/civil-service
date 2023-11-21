data "azurerm_key_vault" "cmc_vault" {
  name                = "cmc-${var.env}"
  resource_group_name = "cmc-${var.env}"
}

resource "azurerm_key_vault_secret" "cmc-db-password-v15" {
  name         = "cmc-db-password-v15"
  value        = module.db-v15.password
  key_vault_id = data.azurerm_key_vault.cmc_vault.id
}

resource "azurerm_key_vault_secret" "cmc-db-username-v15" {
  name         = "cmc-db-username-v15"
  value        = module.db-v15.username
  key_vault_id = data.azurerm_key_vault.cmc_vault.id}

resource "azurerm_key_vault_secret" "cmc-db-host-v15" {
  name         = "cmc-db-host-v15"
  value        = module.db-v15.fqdn
  key_vault_id = data.azurerm_key_vault.cmc_vault.id
}
