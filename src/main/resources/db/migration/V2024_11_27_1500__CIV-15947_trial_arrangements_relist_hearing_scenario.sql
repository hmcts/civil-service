/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Trial.Arrangements.Relist.Hearing.Claimant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}','{}'),
       ('Scenario.AAA6.CP.Trial.Arrangements.Relist.Hearing.Defendant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Required.Defendant"}','{}');

/**
 * Add task item template
 */

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Trial.Arrangements.Relist.Hearing.Claimant', '{1, 1}', 'CLAIMANT', 11, null, null),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Trial.Arrangements.Relist.Hearing.Defendant', '{1, 1}', 'DEFENDANT', 11, null, null);
