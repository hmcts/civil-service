data "azurerm_postgresql_flexible_server" "cmc_db" {
  name                = "cmc-db-v15-${var.env}"
  resource_group_name = local.resource_group_name
}

# ================================================================================================
# Metric Alerts (CPU, Memory, Storage)
# ================================================================================================
resource "azurerm_monitor_metric_alert" "civil-ci-alerts" {
  for_each            = var.monitor_cmc_db_metric_alerts
  name                = "${each.key}-${var.env}"
  resource_group_name = local.resource_group_name
  description         = each.value.description
  severity            = each.value.severity
  enabled             = true
  frequency           = each.value.frequency
  window_size         = each.value.window_size
  auto_mitigate       = true
  scopes              = [data.azurerm_postgresql_flexible_server.cmc_db.id]
  tags                = var.common_tags

  dynamic "criteria" {
    for_each = try(each.value.criteria, [])
    content {
      metric_namespace       = criteria.value.metricNamespace
      metric_name            = criteria.value.metricName
      threshold              = criteria.value.threshold
      operator               = criteria.value.operator
      aggregation            = criteria.value.aggregation
      skip_metric_validation = false
    }
  }

  dynamic "action" {
    for_each = try(each.value.action, [])
    content {
      action_group_id = azurerm_monitor_action_group.civil-service-action-group[action.value.action_group_name].id
    }
  }

  depends_on = [
    azurerm_monitor_action_group.civil-service-action-group,
  ]
}

# ================================================================================================
# Resource Health Alert
# ================================================================================================
resource "azurerm_monitor_activity_log_alert" "cmc_db_resource_health" {
  name                = "cmc-db-resource-health-${var.env}"
  resource_group_name = local.resource_group_name
  scopes = [data.azurerm_postgresql_flexible_server.cmc_db.id]
  description         = "Triggers when CMC PostgreSQL Flexible Server health is Degraded or Unavailable in ${var.env}."
  enabled             = true
  tags                = var.common_tags

  criteria {
    category = "ResourceHealth"

    resource_health {
      current = ["Degraded", "Unavailable"]
      previous = ["Available"]
      reason = ["PlatformInitiated", "UserInitiated"]
    }
  }

  action {
    action_group_id = azurerm_monitor_action_group.civil-service-action-group[action.value.action_group_name].id
  }

  depends_on = [
    azurerm_monitor_action_group.civil-service-action-group,
  ]
  location = var.location
}
