/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.Response.Required', '{}', '{"Notice.AAA7.ClaimIssue.Response.Required" : ["ccdCaseReference", "defaultRespondTime", "responseDeadlineEn", "responseDeadlineCy", "daysLeftToRespond"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimIssue.Response.Required', 'You haven´t responded to the claim', 'You haven´t responded to the claim (CY)',
        'You need to respond before ${defaultRespondTime} on ${responseDeadlineEn}. There are {daysLeftToRespond} days remaining. <a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">Respond to the claim</a>.',
        'You need to respond before ${defaultRespondTime} on ${responseDeadlineCy}. There are {daysLeftToRespond} days remaining. <a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">Respond to the claim</a>.',
        'DEFENDANT');

/**
 * Add task list items
  TODO: update links in href
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_CLAIM_URL}  rel="noopener noreferrer" class="govuk-link">View the claim</a>', 'The claim','<a href={VIEW_CLAIM_URL}>View the claim</a>',
        'The claim', 'Claim.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{3, 3}', 'DEFENDANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>', 'The claim','<a href={VIEW_INFO_ABOUT_CLAIMANT}>View information about the claimant</a>',
        'The claim', 'Claim.Claimant.Info', 'Scenario.AAA7.ClaimIssue.Response.Required', '{3, 3}', 'DEFENDANT', 2),
       ('<a>View the response to the claim</a>', 'The response','<a>View the response to the claim</a>',
        'The response', 'Response.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 3),
       ('<a href={VIEW_INFO_ABOUT_DEFENDANT}  rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response','<a href={VIEW_INFO_ABOUT_DEFENDANT}>View information about the defendant</a>',
        'The response', 'Response.Defendant.Info', 'Scenario.AAA7.ClaimIssue.Response.Required', '{3, 3}', 'DEFENDANT', 4),
       ('<a>View hearings</a>', 'Hearings','<a>View hearings</a>',
        'Hearings', 'Hearing.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 5),
       ('<a>Upload hearing documents</a>', 'Hearings' ,'<a>Upload hearing documents</a>',
        'Hearings', 'Hearing.Document.Upload', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 6),
       ('<a>Add the trial arrangements</a>', 'Hearings' ,'<a>Add the trial arrangements</a>',
        'Hearings', 'Hearing.Arrangements.Add', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 7),
       ('<a>View the bundle</a>', 'Hearings' ,'<a>View the bundle</a>',
        'Hearings', 'Hearing.Bundle.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 8),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,'<a href={VIEW_ORDERS_AND_NOTICES}>View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{3, 3}', 'DEFENDANT', 9),
       ('<a>View the judgment</a>', 'Judgments from the court' ,'<a>View the judgment</a>',
        'Judgments from the court', 'Judgment.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 10),
       ('<a>View applications</a>', 'Applications' ,'<a>View applications</a>',
        'Applications', 'Application.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 11);
