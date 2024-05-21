/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.SetAsideError.Defendant',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant"}',
        '{"Notice.AAA6.JudgmentsOnline.SetAsideError.Defendant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.SetAsideError.Defendant',
        'The judgment made against you has been set aside (removed)',
        'The judgment made against you has been set aside (removed)',
        '<p class="govuk-body">You’ll receive an update with information about next steps.</p>',
        '<p class="govuk-body">You’ll receive an update with information about next steps.</p>',
        'DEFENDANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View the response to the claim</a>', 'The response',
        '<a>View the response to the claim</a>', 'The response',
        'Response.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Defendant', '{3, 3}', 'DEFENDANT', 3),
       ('<a>View hearings</a>', 'Hearing',
        '<a>View hearings</a>', 'Hearing',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Defendant', '{2, 2}', 'DEFENDANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Upload hearing documents</a>', 'Hearing',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Defendant', '{2, 2}', 'DEFENDANT', 10),
       ('<a>View documents</a>', 'Hearing',
        '<a>View documents</a>', 'Hearing',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Defendant', '{2, 2}', 'DEFENDANT', 11),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Add the trial arrangements</a>', 'Hearing',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Defendant', '{2, 2}', 'DEFENDANT', 12),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>View the bundle</a>', 'Hearing',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Defendant', '{2, 2}', 'DEFENDANT', 13),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Defendant', '{3, 3}', 'CLAIMANT', 14),
       ('<a href={VIEW_JUDGEMENT} class="govuk-link">View the judgment</a>', 'Judgments from the court',
        '<a href={VIEW_JUDGEMENT} class="govuk-link">View the judgment</a>', 'Judgments from the court',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Defendant', '{1,1}', 'DEFENDANT', 15);
