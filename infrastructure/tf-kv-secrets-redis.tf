locals {
  redis_kv_env = var.env == "preview" ? "aat" : var.env
}

data "azurerm_key_vault" "citizen_ui_key_vault" {
  name                = "civil-citizen-ui-${local.redis_kv_env}"
  resource_group_name = "civil-citizen-ui-${local.redis_kv_env}"
}

data "azurerm_key_vault_secret" "managed_redis_access_key" {
  key_vault_id = data.azurerm_key_vault.citizen_ui_key_vault.id
  name         = "managed-redis-access-key"
}

resource "azurerm_key_vault_secret" "managed_redis_access_key" {
  name         = "managed-redis-access-key"
  value        = data.azurerm_key_vault_secret.managed_redis_access_key.value
  key_vault_id = data.azurerm_key_vault.civil_key_vault.id
  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "civil-citizen-ui managed-redis"
  })
}
