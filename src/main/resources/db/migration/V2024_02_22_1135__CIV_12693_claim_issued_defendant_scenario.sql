/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.Response.Required', '{}', '{"Notice.AAA6.ClaimIssue.Response.Required" : ["ccdCaseReference", "defaultRespondTime", "respondent1ResponseDeadlineEn", "respondent1ResponseDeadlineCy", "daysLeftToRespond"]}'),
       ('Scenario.AAA6.ClaimIssue.Defendant.FastTrack', '{}', '{}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.Response.Required', 'You haven''t responded to the claim', 'Nid ydych wedi ymateb i''r hawliad',
        '<p class="govuk-body">You need to respond before ${defaultRespondTime} on ${respondent1ResponseDeadlineEn}. There are {daysLeftToRespond} days remaining.</p><p class="govuk-body"><a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">Respond to the claim</a></p>',
        '<p class="govuk-body">Mae angen i chi ymateb cyn ${defaultRespondTime} ar ${respondent1ResponseDeadlineCy}. Mae yna {daysLeftToRespond} diwrnod yn weddill.</p><p class="govuk-body"><a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">Ymateb i''r hawliad</a></p>',
        'DEFENDANT');

/**
 * Add task list items
  TODO: update links in href
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_CLAIM_URL} rel="noopener noreferrer" class="govuk-link">View the claim</a>', 'The claim',
        '<a href={VIEW_CLAIM_URL} rel="noopener noreferrer" class="govuk-link">Gweld yr hawliad</a>',
        'Yr hawliad', 'Claim.View', 'Scenario.AAA6.ClaimIssue.Response.Required', '{3, 3}', 'DEFENDANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT} rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>', 'The claim',
        '<a href={VIEW_INFO_ABOUT_CLAIMANT} rel="noopener noreferrer" class="govuk-link">Gweld gwybodaeth am yr hawliad</a>',
        'Yr hawliad', 'Claim.Claimant.Info', 'Scenario.AAA6.ClaimIssue.Response.Required', '{3, 3}', 'DEFENDANT', 2),
       ('<a>View the response to the claim</a>', 'The response','<a>Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 3),
       ('<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response',
        '<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">Gweld gwybodaeth am y diffynnydd</a>',
        'Yr ymateb', 'Response.Defendant.Info', 'Scenario.AAA6.ClaimIssue.Response.Required', '{3, 3}', 'DEFENDANT', 4),
       ('<a>View mediation settlement agreement</a>', 'Mediation','<a>Gweld cytundeb setlo o ran cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 5),
       ('<a>Upload mediation documents</a>', 'Mediation','<a>Uwchlwytho dogfennau cyfryngu</a>',
        'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 6),
       ('<a>View mediation documents</a>', 'Mediation','<a>Gweld dogfennau cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 7),
       ('<a>View the hearing</a>', 'Hearing','<a>Gweld y gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.View', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 9),
       ('<a>View documents</a>', 'Hearing' ,'<a>Gweld y dogfennau</a>', 'Gwrandawiad',
        'Hearing.Document.View', 'Scenario.AAA6.ClaimIssue.Response.Required','{1, 1}', 'DEFENDANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.ClaimIssue.Defendant.FastTrack', '{1, 1}', 'DEFENDANT', 11),
       ('<a>View the bundle</a>', 'Hearing' ,'<a>Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 12),
       ('<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>',
        'Gorchmynion a rhybuddion gan y llys', 'Order.View', 'Scenario.AAA6.ClaimIssue.Response.Required', '{3, 3}', 'DEFENDANT', 13),
       ('<a>View the judgment</a>', 'Judgments from the court' ,'<a>Gweld y Dyfarniad</a>',
        'Dyfarniadau gan y llys', 'Judgment.View', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 14),
       ('<a>Confirm you''ve paid a judgment (CCJ) debt</a>', 'Judgments from the court','<a>Cadarnhewch eich bod wedi talu dyled dyfarniad (CCJ)</a>',
        'Dyfarniadau gan y llys', 'Judgment.Cosc', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 15),
       ('<a href={GENERAL_APPLICATIONS_INITIATION_PAGE_URL} rel="noopener noreferrer" class="govuk-link">Contact the court to request a change to my case</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_INITIATION_PAGE_URL} rel="noopener noreferrer" class="govuk-link">Contact the court to request a change to my case</a>',
        'Ceisiadau', 'Application.Create', 'Scenario.AAA6.ClaimIssue.Response.Required', '{4, 4}', 'DEFENDANT', 16),
       ('<a>View applications</a>', 'Applications' ,'<a>Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 17);
