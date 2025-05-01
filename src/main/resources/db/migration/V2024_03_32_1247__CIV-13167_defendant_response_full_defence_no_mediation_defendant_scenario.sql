/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant',
        '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant', 'Response to the claim', 'Ymateb i’r hawliad',
        '<p class="govuk-body">You have rejected the claim and refused mediation. The court will contact you when they respond.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a></p>',
        '<p class="govuk-body">Rydych wedi gwrthod yr hawliad ac wedi gwrthod cyfryngu. Bydd y llys yn cysylltu â chi pan fyddant yn ymateb.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">Gweld eich ymateb</a></p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant', '{3, 3}', 'DEFENDANT', 3);
