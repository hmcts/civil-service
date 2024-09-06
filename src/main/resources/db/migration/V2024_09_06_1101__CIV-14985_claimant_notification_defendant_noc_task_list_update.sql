/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefendantNoticeOfChange.Claimant.HearingFee.TaskList', '{}', '{}');

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefendantNoticeOfChange.Claimant.Trial.Arrangements.TaskList', '{}', '{}');


INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
VALUES ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.DefendantNoticeOfChange.Claimant.HearingFee.TaskList', '{2, 2}', 'CLAIMANT', 9, null, null),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.DefendantNoticeOfChange.Claimant.Trial.Arrangements.TaskList', '{2, 2}', 'CLAIMANT', 12, null, null);
