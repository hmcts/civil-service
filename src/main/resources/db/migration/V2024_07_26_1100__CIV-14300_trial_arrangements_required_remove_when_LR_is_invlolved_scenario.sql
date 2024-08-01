/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Lr.Claimant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Required.Defendant"}',
        '{}');

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Lr.Claimant', '{7, 7}', 'DEFENDANT', 11);
