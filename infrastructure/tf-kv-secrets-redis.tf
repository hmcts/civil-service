resource "azurerm_key_vault_secret" "redis_access_key" {
  name         = "civil-service-redis-access-key"
  value        = module.civil-service-managed-redis.primary_access_key
  key_vault_id = data.azurerm_key_vault.civil_key_vault.id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "managed-redis ${module.civil-service-managed-redis.hostname}"
  })
}

resource "azurerm_key_vault_secret" "redis_hostname" {
  name         = "civil-service-redis-hostname"
  value        = module.civil-service-managed-redis.hostname
  key_vault_id = data.azurerm_key_vault.civil_key_vault.id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "managed-redis"
  })
}
