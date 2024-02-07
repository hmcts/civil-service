module "camunda-process-stuck-alert" {
  source            = "git@github.com:hmcts/cnp-module-metric-alert"
  location          = var.appinsights_location
  app_insights_name = module.application_insights.name

  alert_name                 = "camunda-process-stuck-alert"
  alert_desc                 = "Triggers when an Camunda business logic fails resulting in a case getting stuck"
  app_insights_query         = "traces | where message contains \"is not allowed on the case\""
  frequency_in_minutes       = "5"
  time_window_in_minutes     = "5"
  severity_level             = "1"
  action_group_name          = module.civil-fail-action-group-slack.action_group_name
  custom_email_subject       = "Stuck Case Alert"
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = "0"
  resourcegroup_name         = azurerm_resource_group.rg.name
  common_tags                = var.common_tags
  count                      = var.custom_alerts_enabled
}

data "azurerm_key_vault_secret" "slackmonitoringaddress" {
  name         = "slackmonitoringaddress"
  key_vault_id = data.azurerm_key_vault.civil_key_vault.id
}

module "civil-fail-action-group-slack" {
  source                     = "git@github.com:hmcts/cnp-module-action-group"
  location                   = "global"
  environment                = "${var.env}"

  resourcegroup_name     = azurerm_resource_group.rg.name
  action_group_name      = "Civil Fail Slack Alert - ${var.env}"
  short_name             = "Civil_slack"
  email_receiver_name    = "Civil Alerts"
  email_receiver_address = data.azurerm_key_vault_secret.slackmonitoringaddress.value

}
