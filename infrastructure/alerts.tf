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
  env                =  var.env

  resourcegroup_name     = azurerm_resource_group.rg.name
  action_group_name      = "Civil Fail Slack Alert - ${var.env}"
  short_name             = "Civil_slack"
  email_receiver_name    = "Civil Alerts"
  email_receiver_address = data.azurerm_key_vault_secret.slackmonitoringaddress.value

}

module "slack-alerts-storage-account" {
  source           = "git@github.com:hmcts/cnp-module-storage-account?ref=master"
  env      = var.env
  location = var.appinsights_location
  account_kind = "StorageV2"
  account_replication_type = "LRS"
  resource_group_name = azurerm_resource_group.rg.name
  storage_account_name = "civilslackalerts${var.env}"
  common_tags = var.common_tags
}

data "archive_file" "function_app_data" {
  type       = "zip"
  source_dir  = "${path.module}/slack-alerts-data"
  output_path = "${path.module}/function-app.zip"
}


module "civil-camunda-stuck-alert-function-app" {
  source           = "git@github.com:hmcts/cpp-module-terraform-azurerm-functionapp.git"
  environment      = var.env
  location         = var.appinsights_location
  asp_os_type      = "Linux"
  function_app_name = "civilcamundastuckalert${var.env}"
  resource_group_name = azurerm_resource_group.rg.name
  storage_account_access_key = module.slack-alerts-storage-account.storage_account_access_key
  storage_account_name = module.slack-alerts-storage-account.storage_account_name
  key_vault_id = data.azurerm_key_vault.civil_key_vault.id
  tags = var.common_tags + {
    expiresAfter = "3000-01-01"
  }
  functionapp_package = data.archive_file.function_app_data.output_path

}
