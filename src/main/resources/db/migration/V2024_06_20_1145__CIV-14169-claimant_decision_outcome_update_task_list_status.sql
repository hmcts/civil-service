/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Claimant.DecisionOutcome', '{"Notice.AAA6.CP.HearingDocuments.Upload.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}', '{}'),
       ('Scenario.AAA6.Claimant.TrialReady.DecisionOutcome', '{"Notice.AAA6.CP.HearingDocuments.Upload.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}', '{}');


/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
VALUES ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.Claimant.DecisionOutcome', '{2, 2}', 'CLAIMANT', 10, NULL, NULL),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.Claimant.TrialReady.DecisionOutcome', '{2, 2}', 'CLAIMANT', 10, NULL, NULL),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.Claimant.DecisionOutcome', '{2, 2}', 'CLAIMANT', 11, NULL, NULL);
