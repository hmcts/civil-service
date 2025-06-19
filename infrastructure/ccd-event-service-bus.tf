data "azurerm_servicebus_namespace" "ccd-servicebus" {
  name                = "ccd-servicebus-${var.env}"
  resource_group_name = "ccd-shared-${var.env}"
}

module "servicebus-subscription-ccd-events" {
  source       = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=4.x"
  name         = "civil-ccd-case-events-sub-${var.env}"
  namespace_id = data.azurerm_servicebus_namespace.ccd-servicebus.id
  topic_name   = "ccd-case-events-${var.env}"
  status       = var.ccd_service_bus_status
}

resource "azurerm_servicebus_subscription_rule" "civil_ccd_jurisdiction_rule" {
  name            = "civil-ccd-event-rule-${var.env}"
  subscription_id = module.servicebus-subscription-ccd-events.id
  filter_type     = "SqlFilter"
  sql_filter      = var.ccd_service_bus_filter_rule
}
