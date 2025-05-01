/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Update.Defendant.TaskList.UploadDocuments.FinalOrders', '{}', '{"Notice.AAA6.CP.OrderMade.Defendant" : ["orderDocument"]}');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Llwytho dogfennau''r gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.Document.Upload', 'Scenario.AAA6.Update.Defendant.TaskList.UploadDocuments.FinalOrders', '{2, 2}', 'DEFENDANT', 10);
