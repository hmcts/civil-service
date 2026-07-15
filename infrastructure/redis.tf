data "azurerm_subnet" "core_infra_redis_subnet" {
  name                 = "core-infra-subnet-1-${var.env}"
  virtual_network_name = "core-infra-vnet-${var.env}"
  resource_group_name  = "core-infra-${var.env}"
}

module "managed_redis" {
  source = "git@github.com:hmcts/terraform-module-azure-managed-redis?ref=main"

  product     = var.product
  component   = var.component
  env         = var.env
  location    = var.location
  common_tags = var.common_tags

  sku_name          = var.managed_redis_sku
  clustering_policy = "EnterpriseCluster"

  public_network_access       = "Disabled"
  create_private_endpoint     = true
  subnet_id                   = data.azurerm_subnet.core_infra_redis_subnet.id
  private_dns_zone_ids = [
    "/subscriptions/${var.private_dns_subscription_id}/resourceGroups/core-infra-intsvc-rg/providers/Microsoft.Network/privateDnsZones/privatelink.redis.azure.net"
  ]

  access_keys_authentication_enabled = true
}

resource "azurerm_key_vault_secret" "managed_redis_access_key" {
  name         = "managed-redis-access-key"
  value        = module.managed_redis.primary_access_key
  key_vault_id = module.key-vault.key_vault_id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "managed-redis ${module.managed_redis.hostname}"
  })

  depends_on = [module.key-vault]
}
