/**
 * Add scenarios
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Fee.Paid.Task', '{}', '{}'),
       ('Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Tasks.Claimant', '{}', '{}'),
       ('Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Tasks.Defendant', '{}', '{}');

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View the hearing</a>', 'Hearing', '<a>Gweld y gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.View', 'Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Tasks.Claimant', '{1, 1}', 'CLAIMANT', 8),
       ('<a>Pay the hearing fee</a>', 'Hearing', '<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Fee.Paid.Task', '{1, 1}', 'CLAIMANT', 9),
       ('<a>Add the trial arrangements</a>', 'Hearing', '<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Tasks.Claimant', '{1, 1}', 'CLAIMANT', 12),
       ('<a>View the bundle</a>', 'Hearing', '<a>Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Tasks.Claimant', '{1, 1}', 'CLAIMANT', 13),
       ('<a>View the hearing</a>', 'Hearing', '<a>Gweld y gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.View', 'Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Tasks.Defendant', '{1, 1}', 'DEFENDANT', 8),
       ('<a>Add the trial arrangements</a>', 'Hearing', '<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Tasks.Defendant', '{1, 1}', 'DEFENDANT', 12),
       ('<a>View the bundle</a>', 'Hearing', '<a>Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Stay.Lifted.Reset.Hearing.Tasks.Defendant', '{1, 1}', 'DEFENDANT', 13);
