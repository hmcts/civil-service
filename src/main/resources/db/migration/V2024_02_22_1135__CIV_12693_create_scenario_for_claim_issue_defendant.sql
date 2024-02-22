/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.Response.Required', '{}', '{"Notice.AAA7.ClaimIssue.Response.Required" : []}');
/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimIssue.Response.Required', 'You haven´t responded to the claim', 'You haven''t responded to the claim (CY)',
        'You need to respond before ${time} on <Date>. There are ${daysLeftToRespond} days remaining. <a href="#">Respond to the claim.</a>.',
        'You need to respond before <Time> on <Date>. There are ${daysLeftToRespond} days remaining. <a href="#">Respond to the claim.</a>.',
        'defendant');
/**
 * Add task list items
 */

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role,task_order)
values ('<a href=#>View the claim</a>', 'The claim','<a href=#>View the claim</a>',
        'The claim', 'Claim.View', 'Scenario.AAA7.ClaimIssue.ClaimSubmit.Required', '{1, 1}', 'claimant', 1),
       ('<a href=#>View information about the claimant</a>', 'The claim','<a href=#>View information about the claimant</a>',
        'The claim', 'Claim.Claimant.Info', 'Scenario.AAA7.ClaimIssue.ClaimSubmit.Required', '{1, 1}', 'claimant', 2),
       ('<a href=#>View information about the defendant</a>', 'The response','<a href=#>View information about the defendant</a>',
        'The response', 'Response.Defendant.Info', 'Scenario.AAA7.ClaimIssue.ClaimSubmit.Required', '{1, 1}', 'claimant', 4),
       ('<a href=#>View orders and notices</a>', 'Orders and notices from the court' ,'<a href=#>View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA7.ClaimIssue.ClaimSubmit.Required', '{1, 1}', 'claimant', 10);

