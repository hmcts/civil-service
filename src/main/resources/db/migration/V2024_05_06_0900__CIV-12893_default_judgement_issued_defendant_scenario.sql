/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant"}',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant": ["djDefendantNotificationMessage"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant',
        'A judgment has been made against you',
        'A judgment has been made against you',
        '<p class="govuk-body">The exact details of what you need to pay, and by when, are stated on the judgment.   If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can ${djDefendantNotificationMessage}.</p>',
        '<p class="govuk-body">The exact details of what you need to pay, and by when, are stated on the judgment.   If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can ${djDefendantNotificationMessage}.</p>',
        'DEFENDANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View the response to the claim</a>', 'The response',
        '<a>View the response to the claim</a>', 'The response',
        'Response.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 3),
       ('<a>View hearings</a>', 'Hearing',
        '<a>View hearings</a>', 'Hearing',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 5),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Upload hearing documents</a>', 'Hearing',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 6),
       ('<a>View documents</a>', 'Hearing',
        '<a>View documents</a>', 'Hearing',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 7),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Add the trial arrangements</a>', 'Hearing',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 8),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>View the bundle</a>', 'Hearing',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 10),
       ('<a href={VIEW_JUDGEMENT} class="govuk-link">View the judgment</a>', 'Judgments from the court',
        '<a href={VIEW_JUDGEMENT} class="govuk-link">View the judgment</a>', 'Judgments from the court',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{3, 3}', 'DEFENDANT', 12);
