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
VALUES ('Notice.AAA6.ClaimIssue.HWF.Requested', 'We''re reviewing your help with fees application' , 'Rydym yn adolygu eich cais am help i dalu ffioedd',
        '<p class="govuk-body">You''ve applied for help with the claim fee. You''ll receive an update in 5 to 10 working days.</p>',
        '<p class="govuk-body">Fe wnaethoch gais am help i dalu ffi’r hawliad. Byddwch yn cael diweddariad mewn 5 i 10 diwrnod gwaith.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role,task_order)
values ('<a href={VIEW_CLAIM_URL}  rel="noopener noreferrer" class="govuk-link">View the claim</a>', 'The claim',
        '<a href={VIEW_CLAIM_URL}  rel="noopener noreferrer" class="govuk-link">Gweld yr hawliad</a>',
        'Yr hawliad', 'Claim.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{3, 3}', 'CLAIMANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>', 'The claim',
        '<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel="noopener noreferrer" class="govuk-link">Gweld gwybodaeth am yr hawliad</a>',
        'Yr hawliad', 'Claim.Claimant.Info', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{3, 3}', 'CLAIMANT', 2),
       ('<a>View the response to the claim</a>', 'The response','<a>Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 3),
       ('<a>View information about the defendant</a>', 'The response',
        '<a>Gweld gwybodaeth am y diffynnydd</a>',
        'Yr ymateb', 'Response.Defendant.Info', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 4),
       ('<a>View mediation settlement agreement</a>', 'Mediation','<a>View mediation settlement agreement</a>',
        'Mediation', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 5),
       ('<a>Upload mediation documents</a>', 'Mediation','<a>Upload mediation documents</a>',
        'Mediation', 'Upload.Mediation.Documents', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 6),
       ('<a>View mediation documents</a>', 'Mediation','<a>View mediation documents</a>',
        'Mediation', 'View.Mediation.Documents', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 7),
       ('<a>View the hearing</a>', 'Hearing','<a>Gweld y gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 8),
       ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 9),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 10),
       ('<a>View documents</a>', 'Hearing' ,'<a>Gweld y dogfennau</a>',
        'Gwrandawiad', 'Hearing.Document.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested','{1, 1}', 'CLAIMANT', 11),
       ('<a>View the bundle</a>', 'Hearing' ,'<a>Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 13),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>',
        'Gorchmynion a rhybuddion gan y llys', 'Order.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{3, 3}', 'CLAIMANT', 14),
       ('<a>View the judgment</a>', 'Judgments from the court' ,'<a>Gweld y Dyfarniad</a>',
        'Dyfarniad gan y llys', 'Judgment.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 15),
       ('<a>View applications</a>', 'Applications' ,'<a>Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.ClaimIssue.HWF.Requested', '{1, 1}', 'CLAIMANT', 16);
