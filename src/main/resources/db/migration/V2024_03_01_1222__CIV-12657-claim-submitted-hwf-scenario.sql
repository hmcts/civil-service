/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.Requested', '{"Notice.AAA6.ClaimIssue.ClaimSubmit.Required"}',
        '{"Notice.AAA6.ClaimIssue.HWF.Requested" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.Requested', 'We''re reviewing your help with fees application' , 'We''re reviewing your help with fees application',
        '<p class="govuk-body">You''ve applied for help with the claim fee. You''ll receive an update in 5 to 10 working days.</p>',
        '<p class="govuk-body">You''ve applied for help with the claim fee. You''ll receive an update in 5 to 10 working days.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role,task_order)
values ('<a href={VIEW_CLAIM_URL}  rel="noopener noreferrer" class="govuk-link">View the claim</a>', 'The claim',
        '<a href={VIEW_CLAIM_URL}  rel="noopener noreferrer" class="govuk-link">View the claim</a>',
        'The claim', 'Claim.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{3, 3}', 'CLAIMANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>', 'The claim',
        '<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>',
        'The claim', 'Claim.Claimant.Info', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{3, 3}', 'CLAIMANT', 2),
       ('<a>View the response to the claim</a>', 'The response','<a>View the response to the claim</a>',
        'The response', 'Response.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 3),
       ('<a>View information about the defendant</a>', 'The response',
        '<a>View information about the defendant</a>',
        'The response', 'Response.Defendant.Info', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 4),
       ('<a>View mediation settlement agreement</a>', 'Mediation','<a>View mediation settlement agreement</a>',
        'Mediation', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 5),
       ('<a>Upload mediation documents</a>', 'Mediation','<a>Upload mediation documents</a>',
        'Mediation', 'Upload.Mediation.Documents', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 6),
       ('<a>View mediation documents</a>', 'Mediation','<a>View mediation documents</a>',
        'Mediation', 'View.Mediation.Documents', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 7),
       ('<a>View hearings</a>', 'Hearings','<a>View hearings</a>',
        'Hearings', 'Hearing.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearings' ,'<a>Upload hearing documents</a>',
        'Hearings', 'Hearing.Document.Upload', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 9),
       ('<a>Add the trial arrangements</a>', 'Hearings' ,'<a>Add the trial arrangements</a>',
        'Hearings', 'Hearing.Arrangements.Add', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 10),
       ('<a>Pay the hearing fee</a>', 'Hearings' ,'<a>Pay the hearing fee</a>',
        'Hearings', 'Hearing.Fee.Pay', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 11),
       ('<a>View the bundle</a>', 'Hearings' ,'<a>View the bundle</a>',
        'Hearings', 'Hearing.Bundle.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 12),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{3, 3}', 'CLAIMANT', 13),
       ('<a>View the judgment</a>', 'Judgments from the court' ,'<a>View the judgment</a>',
        'Judgments from the court', 'Judgment.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 14),
       ('<a>View applications</a>', 'Applications' ,'<a>View applications</a>',
        'Applications', 'Application.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 15);
