module "servicebus-subscription-ccd-events" {
  source              = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=DTSPO-18682"
  name                = "civil-ccd-case-events-sub-${var.env}"
  namespace_name      = "ccd-servicebus-${var.env}"
  topic_name          = "ccd-case-events-${var.env}"
  resource_group_name = "ccd-shared-${var.env}"
}

resource "azurerm_servicebus_subscription_rule" "civil_ccd_jurisdiction_rule" {
  name            = "civil-ccd-event-rule-${var.env}"
  subscription_id = module.servicebus-subscription-ccd-events.id
  filter_type     = "SqlFilter"
  sql_filter      = "jurisdiction_id IN ('CIVIL','civil')"
}
