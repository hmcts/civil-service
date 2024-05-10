/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{"Notice.AAA6.ClaimIssue.ClaimSubmit.Required"}',
        '{"Notice.AAA6.ClaimIssue.ClaimFee.Required" : ["claimFee"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.ClaimFee.Required', 'You need to pay your claim fee', 'Mae angen i chi dalu ffi eich hawliad',
        '<p class="govuk-body">Your claim has not yet been issued, in order to proceed you must pay the claim fee of ${claimFee}. <a href={CLAIM_FEE_URL}  rel="noopener noreferrer" class="govuk-link">Pay the claim fee</a>.</p>',
        '<p class="govuk-body">Nid yw eich hawliad wedi''i gyhoeddi eto, er mwyn bwrw ymlaen, rhaid i chi dalu ffi’r hawliad o ${claimFee}. <a href={CLAIM_FEE_URL}  rel="noopener noreferrer" class="govuk-link">Talu ffi’r hawliad</a>.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role,task_order)
values ('<a href={VIEW_CLAIM_URL}  rel="noopener noreferrer" class="govuk-link">View the claim</a>', 'The claim',
        '<a href={VIEW_CLAIM_URL}  rel="noopener noreferrer" class="govuk-link">View the claim</a>',
        'The claim', 'Claim.View', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{3, 3}', 'CLAIMANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>', 'The claim',
        '<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>',
        'The claim', 'Claim.Claimant.Info', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{3, 3}', 'CLAIMANT', 2),
       ('<a>View the response to the claim</a>', 'The response','<a>View the response to the claim</a>',
        'The response', 'Response.View', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 3),
       ('<a>View information about the defendant</a>', 'The response','<a>View information about the defendant</a>',
        'The response', 'Response.Defendant.Info', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 4),
       ('<a>View mediation settlement agreement</a>', 'Mediation','<a>View mediation settlement agreement</a>',
        'Mediation', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 5),
       ('<a>Upload mediation documents</a>', 'Mediation','<a>Upload mediation documents</a>',
        'Mediation', 'Upload.Mediation.Documents', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 6),
       ('<a>View mediation documents</a>', 'Mediation','<a>View mediation documents</a>',
        'Mediation', 'View.Mediation.Documents', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 7),
       ('<a>View the hearing</a>', 'Hearing','<a>View the hearing</a>',
        'Hearing', 'Hearing.View', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 8),
       ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Pay the hearing fee</a>',
        'Hearing', 'Hearing.Fee.Pay', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 9),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Upload hearing documents</a>',
        'Hearing', 'Hearing.Document.Upload', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 10),
       ('<a>View documents</a>', 'Hearing' ,'<a>View documents</a>',
        'Hearing', 'Hearing.Document.View', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required','{1, 1}', 'CLAIMANT', 11),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Add the trial arrangements</a>',
        'Hearing', 'Hearing.Arrangements.Add', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 12),
       ('<a>View the bundle</a>', 'Hearing' ,'<a>View the bundle</a>',
        'Hearing', 'Hearing.Bundle.View', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 13),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{3, 3}', 'CLAIMANT', 14),
       ('<a>View the judgment</a>', 'Judgments from the court' ,'<a>View the judgment</a>',
        'Judgments from the court', 'Judgment.View', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 15),
       ('<a>View applications</a>', 'Applications' ,'<a>View applications</a>',
        'Applications', 'Application.View', 'Scenario.AAA6.ClaimIssue.ClaimFee.Required', '{1, 1}', 'CLAIMANT', 16);
