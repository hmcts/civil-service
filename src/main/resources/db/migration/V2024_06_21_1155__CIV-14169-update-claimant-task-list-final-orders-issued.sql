/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Update.Claimant.TaskList.UploadDocuments.FinalOrders', '{"Notice.AAA6.CP.OrderMade.Claimant"}', '{"Notice.AAA6.CP.OrderMade.Claimant.FinalOrders" : ["orderDocument"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.OrderMade.Claimant.FinalOrders', 'An order has been made', 'Mae gorchymyn wedi’i wneud',
        '<p class="govuk-body">The judge has made an order on your claim. <a href="{VIEW_FINAL_ORDER}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the order</a>.</p>',
        '<p class="govuk-body">Mae''r Barnwr wedi gwneud gorchymyn ar eich hawliad. <a href="{VIEW_FINAL_ORDER}" rel="noopener noreferrer" target="_blank" class="govuk-link">Gweld y gorchymyn</a>.</p>',
        'CLAIMANT', 'Session');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Llwytho dogfennau''r gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.Document.Upload', 'Scenario.AAA6.Update.Claimant.TaskList.UploadDocuments.FinalOrders', '{2, 2}', 'CLAIMANT', 10);
