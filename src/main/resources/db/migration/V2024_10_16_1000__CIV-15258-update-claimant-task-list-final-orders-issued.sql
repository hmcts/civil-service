/**
 * Update scenario with new notifications_to_delete
 */
UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}'
WHERE name = 'Scenario.AAA6.Update.Claimant.TaskList.UploadDocuments.FinalOrders';

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Update.TaskList.TrialReady.FinalOrders.Claimant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}',
        '{"Notice.AAA6.CP.OrderMade.Claimant" : ["orderDocument"]}');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Llwytho dogfennau''r gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.Document.Upload', 'Scenario.AAA6.Update.TaskList.TrialReady.FinalOrders.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.Update.TaskList.TrialReady.FinalOrders.Claimant', '{2, 2}', 'CLAIMANT', 12);
