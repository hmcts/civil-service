
/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant"}',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant',
        'A judgment against the defendant has been made',
        'A judgment against the defendant has been made',
        '<p class="govuk-body">The defendant should now pay you according to the terms of the judgment. <br> Once they do, you should confirm that they’ve paid you the full amount that you’re owed.<br>If they do not pay you by the date on the judgment, you can ask for enforcement action to be taken against them. <br>If you need to change the terms of payment within the judgment, such as the instalments you had previously agreed, you can make an application to vary the judgment.</p>',
        '<p class="govuk-body">The defendant should now pay you according to the terms of the judgment. <br> Once they do, you should confirm that they’ve paid you the full amount that you’re owed.<br>If they do not pay you by the date on the judgment, you can ask for enforcement action to be taken against them. <br>If you need to change the terms of payment within the judgment, such as the instalments you had previously agreed, you can make an application to vary the judgment.</p>',
        'CLAIMANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View the response to the claim</a>', 'The response',
        '<a>View the response to the claim</a>', 'The response',
        'Response.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant', '{2, 2}', 'CLAIMANT', 3),
       ('<a>View hearings</a>', 'Hearing',
        '<a>View hearings</a>', 'Hearing',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant', '{2, 2}', 'CLAIMANT', 5),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Upload hearing documents</a>', 'Hearing',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant', '{2, 2}', 'CLAIMANT', 6),
       ('<a>View documents</a>', 'Hearing',
        '<a>View documents</a>', 'Hearing',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant', '{2, 2}', 'CLAIMANT', 7),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Add the trial arrangements</a>', 'Hearing',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant', '{2, 2}', 'CLAIMANT', 8),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>View the bundle</a>', 'Hearing',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>View the judgment</a>', 'Judgments from the court',
        '<a>View the judgment</a>', 'Judgments from the court',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant', '{3, 3}', 'CLAIMANT', 12);
