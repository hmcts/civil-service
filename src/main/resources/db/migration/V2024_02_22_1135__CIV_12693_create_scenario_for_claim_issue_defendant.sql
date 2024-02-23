/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.Response.Required', '{}', '{"Notice.AAA7.ClaimIssue.Response.Required" : ["ccdCaseReference", "defaultRespondTime", "responseDeadline", "daysLeftToRespond"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimIssue.Response.Required', 'You haven´t responded to the claim', 'You haven´t responded to the claim (CY)',
        'You need to respond before ${defaultRespondTime} on ${responseDeadline}. There are ${daysLeftToRespond} days remaining. <a href="/case/${ccdCaseReference}/response/task-list">Respond to the claim.</a>.',
        'You need to respond before ${defaultRespondTime} on ${responseDeadline}. There are ${daysLeftToRespond} days remaining. <a href="/case/${ccdCaseReference}/response/task-list">Respond to the claim.</a>.',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href=#>View the claim</a>', 'The claim','<a href=#>View the claim</a>',
        'The claim', 'Claim.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 1),
       ('<a href=#>View information about the claimant</a>', 'The claim','<a href=#>View information about the claimant</a>',
        'The claim', 'Claim.Claimant.Info', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 2),
       ('<a href=#>View the response to the claim</a>', 'The response','<a href=#>View the response to the claim</a>',
        'The response', 'Response.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 3),
       ('<a href=#>View information about the defendant</a>', 'The response','<a href=#>View information about the defendant</a>',
        'The response', 'Response.Defendant.Info', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 4),
       ('<a href=#>View hearings</a>', 'Hearings','<a href=#>View hearings</a>',
        'Hearings', 'Hearing.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 5),
       ('<a href=#>Upload hearing documents</a>', 'Hearings' ,'<a href=#>Upload hearing documents</a>',
        'Hearings', 'Hearing.Document.Upload', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 6),
       ('<a href=#>Add the trial arrangements</a>', 'Hearings' ,'<a href=#>Add the trial arrangements</a>',
        'Hearings', 'Hearing.Arrangements.Add', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 7),
       ('<a href=#>View the bundle</a>', 'Hearings' ,'<a href=#>View the bundle</a>',
        'Hearings', 'Hearing.Bundle.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 8),
       ('<a href=#>View orders and notices</a>', 'Orders and notices from the court' ,'<a href=#>View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 9),
       ('<a href=#>View the judgment</a>', 'Judgments from the court' ,'<a href=#>View the judgment</a>',
        'Judgments from the court', 'Judgment.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 10),
       ('<a href=#>View applications</a>', 'Applications' ,'<a href=#>View applications</a>',
        'Applications', 'Application.View', 'Scenario.AAA7.ClaimIssue.Response.Required', '{1, 1}', 'DEFENDANT', 11);
