mock_provider "azurerm" {
  mock_data "azurerm_client_config" {
    defaults = { subscription_id = "00000000-0000-0000-0000-000000000001" }
  }
}
variables {
  name                = "civil-concurrency-report-aat"
  resource_group_name = "civil-service-aat"
  location            = "UK South"
  tags                = {}
}
run "disabled_workflow_has_scoped_identity_and_monitoring" {
  command = plan
  variables { config = { "enabled" : false, "frequency" : "Month", "interval" : 1, "start_time" : "2027-01-01T08:00:00", "time_zone" : "GMT Standard Time", "definition_json" : "{\"actions\": {\"Synthetic\": {\"type\": \"Compose\", \"inputs\": {\"synthetic\": true}, \"runAfter\": {}}}}", "log_resource_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Insights/components/ccd-test", "artifact_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports/blobServices/default/containers/concurrency", "ledger_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports/blobServices/default/containers/concurrency-state", "action_group_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Insights/actionGroups/operations", "deadline_hours" : 720 } }

  assert {
    condition     = jsondecode(azurerm_resource_group_template_deployment.workflow.template_content).resources[1].properties.state == "Disabled"
    error_message = "New workflows must honour disabled configuration."
  }
  assert {
    condition     = jsondecode(azurerm_resource_group_template_deployment.workflow.template_content).resources[0].properties.parameterValueSet.name == "managedIdentityAuth"
    error_message = "The telemetry connection must use managed identity."
  }
  assert {
    condition     = jsondecode(azurerm_resource_group_template_deployment.workflow.template_content).resources[1].properties.definition.triggers.Recurrence.runtimeConfiguration.concurrency.runs == 1
    error_message = "Overlapping report invocations must be bounded."
  }
  assert {
    condition     = var.config.deadline_hours == 720
    error_message = "Monthly deadline configuration must reach the independent watchdog."
  }
}
run "reject_broad_source" {
  command = plan
  variables { config = { "enabled" : false, "frequency" : "Month", "interval" : 1, "start_time" : "2027-01-01T08:00:00", "time_zone" : "GMT Standard Time", "definition_json" : "{\"actions\": {\"Synthetic\": {\"type\": \"Compose\", \"inputs\": {\"synthetic\": true}, \"runAfter\": {}}}}", "log_resource_id" : "/subscriptions/00000000-0000-0000-0000-000000000001", "artifact_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports/blobServices/default/containers/concurrency", "ledger_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports/blobServices/default/containers/concurrency-state", "action_group_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Insights/actionGroups/operations", "deadline_hours" : 720 } }
  expect_failures = [var.config]
}
run "reject_broad_storage" {
  command = plan
  variables { config = { "enabled" : false, "frequency" : "Month", "interval" : 1, "start_time" : "2027-01-01T08:00:00", "time_zone" : "GMT Standard Time", "definition_json" : "{\"actions\": {\"Synthetic\": {\"type\": \"Compose\", \"inputs\": {\"synthetic\": true}, \"runAfter\": {}}}}", "log_resource_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Insights/components/ccd-test", "artifact_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports/blobServices/default/containers/concurrency", "ledger_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports", "action_group_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Insights/actionGroups/operations", "deadline_hours" : 720 } }
  expect_failures = [var.config]
}
run "reject_utc_suffix_ignores_timezone" {
  command = plan
  variables { config = { "enabled" : false, "frequency" : "Month", "interval" : 1, "start_time" : "2027-01-01T08:00:00Z", "time_zone" : "GMT Standard Time", "definition_json" : "{\"actions\": {\"Synthetic\": {\"type\": \"Compose\", \"inputs\": {\"synthetic\": true}, \"runAfter\": {}}}}", "log_resource_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Insights/components/ccd-test", "artifact_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports/blobServices/default/containers/concurrency", "ledger_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports/blobServices/default/containers/concurrency-state", "action_group_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Insights/actionGroups/operations", "deadline_hours" : 720 } }
  expect_failures = [var.config]
}
run "reject_no_actions" {
  command = plan
  variables { config = { "enabled" : false, "frequency" : "Month", "interval" : 1, "start_time" : "2027-01-01T08:00:00", "time_zone" : "GMT Standard Time", "definition_json" : "{\"actions\":{}}", "log_resource_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Insights/components/ccd-test", "artifact_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports/blobServices/default/containers/concurrency", "ledger_container_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Storage/storageAccounts/reports/blobServices/default/containers/concurrency-state", "action_group_id" : "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/reporting/providers/Microsoft.Insights/actionGroups/operations", "deadline_hours" : 720 } }
  expect_failures = [var.config]
}
