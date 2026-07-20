import {
  for_each = var.env == "aat" ? toset(["import"]) : toset([])
  to       = module.managed_redis.azurerm_managed_redis.redis
  id       = "/subscriptions/1c4f0704-a29e-403d-b719-b90c34ef14c9/resourceGroups/civil-service-aat-rg/providers/Microsoft.Cache/redisEnterprise/civil-service-aat"
}
