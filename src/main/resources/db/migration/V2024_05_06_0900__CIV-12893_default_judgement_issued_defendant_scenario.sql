/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant","Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant": ["djDefendantNotificationMessage", "djDefendantNotificationMessageCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant',
        'A judgment has been made against you',
        'Mae Dyfarniad wedi’i wneud yn eich erbyn',
        '<p class="govuk-body">The exact details of what you need to pay, and by when, are stated on the judgment.   If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can ${djDefendantNotificationMessage}.</p>',
        '<p class="govuk-body">Mae union fanylion yr hyn mae arnoch angen ei dalu, ac erbyn pryd, wedi’u nodi ar y dyfarniad. Os ydych eisiau gwrthwynebu’r dyfarniad, neu ofyn i newid pryd a sut y byddwch yn talu swm yr hawliad yn ôl, gallwch ${djDefendantNotificationMessageCy}.</p>',
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
        '<a href={VIEW_JUDGEMENT} class="govuk-link">Gweld y Dyfarniad</a>', 'Dyfarniadau gan y llys',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{3, 3}', 'DEFENDANT', 14),
       ('<a href={COMFIRM_YOU_PAID_JUDGMENT_DEBT} class="govuk-link">Confirm you''ve paid a judgment (CCJ) debt</a>', 'Judgments from the court',
        '<a href={COMFIRM_YOU_PAID_JUDGMENT_DEBT} class="govuk-link">Cadarnhewch eich bod wedi talu dyled dyfarniad (CCJ)</a>', 'Dyfarniadau gan y llys',
        'Judgment.Cosc', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{3, 3}', 'DEFENDANT', 15);
