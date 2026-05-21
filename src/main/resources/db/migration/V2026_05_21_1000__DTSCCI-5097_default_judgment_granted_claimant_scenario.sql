INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant"}',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant": ["djClaimantNotificationMessage", "djClaimantNotificationMessageCy"]}');

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View the response to the claim</a>', 'The response',
        '<a>View the response to the claim</a>', 'The response',
        'Response.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant', '{2, 2}', 'CLAIMANT', 3),
       ('<a>View hearings</a>', 'Hearing',
        '<a>View hearings</a>', 'Hearing',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant', '{2, 2}', 'CLAIMANT', 5),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Upload hearing documents</a>', 'Hearing',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant', '{2, 2}', 'CLAIMANT', 6),
       ('<a>View documents</a>', 'Hearing',
        '<a>View documents</a>', 'Hearing',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant', '{2, 2}', 'CLAIMANT', 7),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Add the trial arrangements</a>', 'Hearing',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant', '{2, 2}', 'CLAIMANT', 8),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>View the bundle</a>', 'Hearing',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a href="{VIEW_JUDGEMENT}" class="govuk-link">View the judgment</a>', 'Judgments from the court',
        '<a href="{VIEW_JUDGEMENT}" class="govuk-link">View the judgment</a>', 'Judgments from the court',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant', '{3, 3}', 'CLAIMANT', 12);
