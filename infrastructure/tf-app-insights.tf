resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-${var.env}"
  location            = var.appinsights_location
  resource_group_name = azurerm_resource_group.rg.name
  application_type    = "web"
}

resource "azurerm_key_vault_secret" "app_insights_key" {
  name         = "appinsights-instrumentation-key"
  value        = azurerm_application_insights.appinsights.instrumentation_key
  key_vault_id = module.key-vault.key_vault_id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "appinsights ${azurerm_application_insights.appinsights.name}"
  })

  depends_on = [
    module.key-vault
  ]
}