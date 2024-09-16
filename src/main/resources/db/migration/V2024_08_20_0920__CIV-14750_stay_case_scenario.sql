/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Case.Stayed.Claimant',
        '{}',
        '{"Notice.AAA6.CP.Case.Stayed.Claimant" : []}'),
       ('Scenario.AAA6.CP.Case.Stayed.Defendant',
        '{}',
        '{"Notice.AAA6.CP.Case.Stayed.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.Case.Stayed.Claimant', 'The case has been stayed', 'Mae’r achos wedi cael ei atal',
        '<p class="govuk-body">The case has been stayed. This could be as a result of a judge’s order. Any upcoming hearings will be cancelled.</p>',
        '<p class="govuk-body">Mae’r achos wedi’i atal. Gallai hyn fod o ganlyniad i orchymyn a waned gan farnwr. Bydd unrhyw wrandawiadau sydd i ddod yn cael eu canslo.</p>',
        'CLAIMANT'),
       ('Notice.AAA6.CP.Case.Stayed.Defendant', 'The case has been stayed', 'Mae’r achos wedi cael ei atal',
        '<p class="govuk-body">The case has been stayed. This could be as a result of a judge’s order. Any upcoming hearings will be cancelled.</p>',
        '<p class="govuk-body">Mae’r achos wedi’i atal. Gallai hyn fod o ganlyniad i orchymyn a waned gan farnwr. Bydd unrhyw wrandawiadau sydd i ddod yn cael eu canslo.</p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role,task_order)
values ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.Case.Stayed.Claimant', '{2, 2}', 'CLAIMANT', 9),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.Case.Stayed.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.Case.Stayed.Claimant', '{2, 2}', 'CLAIMANT', 9),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.Case.Stayed.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Case.Stayed.Claimant', '{2, 2}', 'CLAIMANT', 11),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.Case.Stayed.Defendant', '{2, 2}', 'DEFENDANT', 10),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.Case.Stayed.Defendant', '{2, 2}', 'DEFENDANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Case.Stayed.Defendant', '{2, 2}', 'DEFENDANT', 11);

