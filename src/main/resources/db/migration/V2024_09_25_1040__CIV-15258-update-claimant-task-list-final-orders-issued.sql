/**
 * Update scenario with new notifications_to_delete
 */
UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}'
WHERE name = 'Scenario.AAA6.Update.Claimant.TaskList.UploadDocuments.FinalOrders';

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.Update.Claimant.TaskList.UploadDocuments.FinalOrders', '{2, 2}', 'CLAIMANT', 12);
