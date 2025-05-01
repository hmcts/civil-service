/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CaseProceedsInCaseman.Claimant', '{}', '{"Notice.AAA6.CaseProceedsInCaseman.Claimant" : []}'),
       ('Scenario.AAA6.CaseProceedsInCaseman.Claimant.FastTrack', '{}', '{"Notice.AAA6.CaseProceedsInCaseman.Claimant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CaseProceedsInCaseman.Claimant', 'Your online account will no longer be updated', 'Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach',
        '<p class="govuk-body">Your online account will no longer be updated. If there are any further updates to your case these will be by post.</p>',
        '<p class="govuk-body">Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach. Os oes unrhyw ddiweddariadau pellach i’ch achos, bydd y rhain yn cael eu hanfon atoch drwy''r post.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role,task_order)
values ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CaseProceedsInCaseman.Claimant', '{2, 2}', 'CLAIMANT', 9),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CaseProceedsInCaseman.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CaseProceedsInCaseman.Claimant.FastTrack', '{2, 2}', 'CLAIMANT', 9),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CaseProceedsInCaseman.Claimant.FastTrack', '{2, 2}', 'CLAIMANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CaseProceedsInCaseman.Claimant.FastTrack', '{2, 2}', 'CLAIMANT', 11);
