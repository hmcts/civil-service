# Independent of the report's trigger and email sender. Runs metadata contains
# no report payload; secure action history also hides the management response.
resource "azurerm_logic_app_workflow" "deadline" {
  name                = "${var.name}-deadline"
  location            = var.location
  resource_group_name = var.resource_group_name
  enabled             = var.config.enabled
  tags                = var.tags
  identity { type = "SystemAssigned" }
}

resource "azurerm_role_assignment" "deadline" {
  scope                            = local.workflow_id
  role_definition_name             = "Reader"
  principal_id                     = azurerm_logic_app_workflow.deadline.identity[0].principal_id
  skip_service_principal_aad_check = true
  depends_on                       = [azurerm_resource_group_template_deployment.workflow]
}

resource "azurerm_logic_app_trigger_recurrence" "deadline" {
  name         = "Recurrence"
  logic_app_id = azurerm_logic_app_workflow.deadline.id
  frequency    = "Hour"
  interval     = 1
}

resource "azurerm_logic_app_action_custom" "last_success" {
  name         = "Find_recent_success"
  logic_app_id = azurerm_logic_app_workflow.deadline.id
  body = jsonencode({
    type = "Http"
    inputs = {
      method = "GET"
      uri    = "https://management.azure.com${local.workflow_id}/runs"
      queries = {
        "api-version" = "2016-06-01"
        "$top"        = "1"
        "$filter"     = "@concat('Status eq ''Succeeded'' and StartTime ge ', addHours(utcNow(), -${var.config.deadline_hours}))"
      }
      authentication = { type = "ManagedServiceIdentity", audience = "https://management.azure.com/" }
      retryPolicy    = { type = "exponential", count = 3, interval = "PT10S" }
    }
    runtimeConfiguration = { secureData = { properties = ["inputs", "outputs"] } }
    runAfter             = {}
  })
  depends_on = [azurerm_logic_app_trigger_recurrence.deadline, azurerm_role_assignment.deadline]
}

resource "azurerm_logic_app_action_custom" "deadline" {
  name         = "Check_deadline"
  logic_app_id = azurerm_logic_app_workflow.deadline.id
  body = jsonencode({
    type       = "If"
    expression = "@empty(body('Find_recent_success')?['value'])"
    actions = {
      Deadline_missed = {
        type = "Terminate"
        inputs = {
          runStatus = "Failed"
          runError  = { code = "ReportDeadlineMissed", message = "No successful report within its configured deadline. Check source completeness and delivery state before recovery." }
        }
        runAfter = {}
      }
    }
    else     = { actions = {} }
    runAfter = { Find_recent_success = ["Succeeded"] }
  })
  depends_on = [azurerm_logic_app_action_custom.last_success]
}

resource "azurerm_monitor_metric_alert" "deadline" {
  for_each = {
    failed = { metric = "RunsFailed", operator = "GreaterThan", threshold = 0, window = "PT1H" }
    absent = { metric = "RunsSucceeded", operator = "LessThan", threshold = 1, window = "PT6H" }
  }
  name                = "${var.name}-deadline-${each.key}"
  resource_group_name = var.resource_group_name
  scopes              = [azurerm_logic_app_workflow.deadline.id]
  description         = "Independent report deadline checker failed or stopped. Check reporting readiness and recover the intended report period."
  severity            = 2
  enabled             = var.config.enabled
  frequency           = "PT5M"
  window_size         = each.value.window
  tags                = var.tags
  criteria {
    metric_namespace = "Microsoft.Logic/workflows"
    metric_name      = each.value.metric
    aggregation      = "Total"
    operator         = each.value.operator
    threshold        = each.value.threshold
  }
  action { action_group_id = var.config.action_group_id }
}
