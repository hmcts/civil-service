locals {
  # Preview shares AAT networking infrastructure
  redis_network_env = var.env == "preview" ? "aat" : var.env
}

data "azurerm_subnet" "core_infra_redis_subnet" {
  name                 = "core-infra-subnet-1-${local.redis_network_env}"
  virtual_network_name = "core-infra-vnet-${local.redis_network_env}"
  resource_group_name  = "core-infra-${local.redis_network_env}"
}

module "civil-service-managed-redis" {
  source = "git@github.com:hmcts/terraform-module-azure-managed-redis?ref=main"

  product     = var.product
  component   = "${var.component}-cache"
  env         = var.env
  location    = var.location
  common_tags = var.common_tags

  public_network_access   = "Disabled"
  create_private_endpoint = true
  subnet_id               = data.azurerm_subnet.core_infra_redis_subnet.id
  private_dns_zone_ids    = [
    "/subscriptions/${var.private_dns_subscription_id}/resourceGroups/core-infra-intsvc-rg/providers/Microsoft.Network/privateDnsZones/privatelink.redis.azure.net"
  ]

  sku_name                           = var.managed_redis_sku
  access_keys_authentication_enabled = true
}
