data "azurerm_subnet" "core_infra_redis_subnet" {
  name                 = "core-infra-subnet-1-${var.env}"
  virtual_network_name = "core-infra-vnet-${var.env}"
  resource_group_name  = "core-infra-${var.env}"
}

module "civil-service-cache" {
  source                          = "git@github.com:hmcts/cnp-module-redis?ref=master"
  product                         = "${var.product}-${var.component}-cache"
  location                        = var.location
  env                             = var.env
  subnetid                        = data.azurerm_subnet.core_infra_redis_subnet.id
  common_tags                     = var.common_tags
  redis_version                   = 6
  private_endpoint_enabled        = true
  business_area                   = "cft"
  public_network_access_enabled   = false
  sku_name                        = var.redis_sku_name
  family                          = var.redis_family
  capacity                        = var.redis_capacity
}
