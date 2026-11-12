terraform {
  required_version = ">= 1.7"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 5.13, < 6.0"
    }
  }
}

data "azurerm_client_config" "current" {}

locals {
  workflow_id   = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/resourceGroups/${var.resource_group_name}/providers/Microsoft.Logic/workflows/${var.name}"
  connector_id  = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/providers/Microsoft.Web/locations/${replace(lower(var.location), " ", "")}/managedApis/azuremonitorlogs"
  connection_id = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/resourceGroups/${var.resource_group_name}/providers/Microsoft.Web/connections/${var.name}-logs"
  # Infrastructure owns identity, connection, schedule and safety settings. Report
  # tickets own actions/parameters, including distributed period/delivery state.
  definition = merge(jsondecode(var.config.definition_json), {
    "$schema"      = "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#"
    contentVersion = "1.0.0.0"
    parameters = merge(try(jsondecode(var.config.definition_json).parameters, {}), {
      "$connections" = { type = "Object", defaultValue = {} }
    })
    triggers = {
      Recurrence = {
        type = "Recurrence"
        recurrence = {
          frequency = var.config.frequency
          interval  = var.config.interval
          startTime = var.config.start_time
          timeZone  = var.config.time_zone
        }
        runtimeConfiguration = { concurrency = { runs = 1, maximumWaitingRuns = 1 } }
      }
    }
  })
}

# ARM is used for the managed-identity connection parameter set, which is not
# exposed by the older AzureRM API connection resource used with this repository.
resource "azurerm_resource_group_template_deployment" "workflow" {
  name                = "${var.name}-infrastructure"
  resource_group_name = var.resource_group_name
  deployment_mode     = "Incremental"
  template_content = jsonencode({
    "$schema"      = "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#"
    contentVersion = "1.0.0.0"
    resources = [
      {
        type       = "Microsoft.Web/connections"
        apiVersion = "2016-06-01"
        name       = "${var.name}-logs"
        location   = var.location
        kind       = "V1"
        tags       = var.tags
        properties = {
          displayName       = "${var.name}-logs"
          api               = { id = local.connector_id }
          parameterValueSet = { name = "managedIdentityAuth", values = {} }
        }
      },
      {
        type       = "Microsoft.Logic/workflows"
        apiVersion = "2019-05-01"
        name       = var.name
        location   = var.location
        tags       = var.tags
        identity   = { type = "SystemAssigned" }
        dependsOn  = [local.connection_id]
        properties = {
          state      = var.config.enabled ? "Enabled" : "Disabled"
          definition = local.definition
          parameters = {
            "$connections" = {
              value = {
                azuremonitorlogs = {
                  connectionId         = local.connection_id
                  connectionName       = "${var.name}-logs"
                  id                   = local.connector_id
                  connectionProperties = { authentication = { type = "ManagedServiceIdentity" } }
                }
              }
            }
          }
          runtimeConfiguration = { lifetime = { unit = "day", count = 40 } }
        }
      }
    ]
    outputs = {
      principalId = {
        type  = "string"
        value = "[reference('${var.name}', '2019-05-01', 'Full').identity.principalId]"
      }
    }
  })
}

resource "azurerm_role_assignment" "telemetry" {
  scope                            = var.config.log_resource_id
  role_definition_name             = "Log Analytics Reader"
  principal_id                     = jsondecode(azurerm_resource_group_template_deployment.workflow.output_content).principalId.value
  skip_service_principal_aad_check = true
}

resource "azurerm_role_assignment" "storage" {
  for_each                         = toset([var.config.artifact_container_id, var.config.ledger_container_id])
  scope                            = each.value
  role_definition_name             = "Storage Blob Data Contributor"
  principal_id                     = jsondecode(azurerm_resource_group_template_deployment.workflow.output_content).principalId.value
  skip_service_principal_aad_check = true
}

resource "azurerm_monitor_metric_alert" "failed" {
  name                = "${var.name}-failed"
  resource_group_name = var.resource_group_name
  scopes              = [local.workflow_id]
  description         = "Support report workflow failure. Investigate generation and per-destination delivery state before replay."
  severity            = 2
  enabled             = var.config.enabled
  frequency           = "PT5M"
  window_size         = "PT15M"
  tags                = var.tags
  criteria {
    metric_namespace = "Microsoft.Logic/workflows"
    metric_name      = "RunsFailed"
    aggregation      = "Total"
    operator         = "GreaterThan"
    threshold        = 0
  }
  action { action_group_id = var.config.action_group_id }
  depends_on = [azurerm_resource_group_template_deployment.workflow]
}

output "infrastructure" {
  value = {
    workflow_id           = local.workflow_id
    logs_connection_id    = local.connection_id
    principal_id          = jsondecode(azurerm_resource_group_template_deployment.workflow.output_content).principalId.value
    artifact_container_id = var.config.artifact_container_id
    ledger_container_id   = var.config.ledger_container_id
  }
}

output "template" {
  description = "Non-secret ARM definition for validation before deployment. Definitions must not contain inline credentials."
  value       = azurerm_resource_group_template_deployment.workflow.template_content
}

output "deadline_hours" {
  value = var.config.deadline_hours
}
