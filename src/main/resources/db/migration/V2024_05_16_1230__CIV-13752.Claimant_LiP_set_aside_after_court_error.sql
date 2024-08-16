/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.SetAsideError.Claimant',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant"}',
        '{"Notice.AAA6.JudgmentsOnline.SetAsideError.Claimant": []}');

/**
 * Add notification template for claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.SetAsideError.Claimant',
        'The judgment against the defendant has been set aside (removed)',
        'Mae’r dyfarniad yn erbyn y diffynnydd wedi cael ei roi o’r naill du (wedi’i ddileu)',
        '<p class="govuk-body">You’ll receive an update with information about next steps.</p>',
        '<p class="govuk-body">Byddwch yn cael diweddariad gyda gwybodaeth am y camau nesaf.</p>',
        'CLAIMANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View the hearing</a>', 'Hearing',
        '<a>View the hearing</a>', 'Hearing',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Claimant', '{2, 2}', 'CLAIMANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Upload hearing documents</a>', 'Hearing',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>View documents</a>', 'Hearing',
        '<a>View documents</a>', 'Hearing',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Claimant', '{2, 2}', 'CLAIMANT', 11),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Add the trial arrangements</a>', 'Hearing',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Claimant', '{2, 2}', 'CLAIMANT', 12),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>View the bundle</a>', 'Hearing',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Claimant', '{2, 2}', 'CLAIMANT', 13),
       ('<a>View the judgment</a>', 'Judgments from the court',
        '<a>View the judgment</a>', 'Judgments from the court',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Claimant', '{1, 1}', 'CLAIMANT', 15);
