resource "azurerm_key_vault_secret" "redis_access_key" {
  name         = "civil-service-redis-access-key"
  value        = module.civil-service-cache.access_key
  key_vault_id = data.azurerm_key_vault.civil_key_vault.id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "redis ${module.civil-service-cache.host_name}"
  })
}
