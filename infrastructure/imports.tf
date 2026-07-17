data "azurerm_client_config" "current" {}

locals {
  redis_name = "${var.product}-${var.component}-${var.env}"
  redis_rg   = "${var.product}-${var.component}-${var.env}-rg"
  sub_id     = data.azurerm_client_config.current.subscription_id
}

import {
  to = module.managed_redis.azurerm_resource_group.rg[0]
  id = "/subscriptions/${local.sub_id}/resourceGroups/${local.redis_rg}"
}

import {
  to = module.managed_redis.azurerm_managed_redis.redis
  id = "/subscriptions/${local.sub_id}/resourceGroups/${local.redis_rg}/providers/Microsoft.Cache/redisEnterprise/${local.redis_name}"
}

import {
  to = module.managed_redis.azurerm_private_endpoint.redis_pe[0]
  id = "/subscriptions/${local.sub_id}/resourceGroups/${local.redis_rg}/providers/Microsoft.Network/privateEndpoints/${local.redis_name}-pe"
}

import {
  to = azurerm_key_vault_secret.managed_redis_access_key
  id = "https://civil-${var.env}.vault.azure.net/secrets/managed-redis-access-key"
}
