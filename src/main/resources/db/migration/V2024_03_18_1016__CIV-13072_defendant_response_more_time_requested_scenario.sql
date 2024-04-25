/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.MoreTimeRequested.Defendant', '{"Notice.AAA6.ClaimIssue.Response.Required"}',
        '{"Notice.AAA6.DefResponse.MoreTimeRequested.Defendant" : ["respondent1ResponseDeadlineEn", "respondent1ResponseDeadlineCy","defaultRespondTime"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.MoreTimeRequested.Defendant', 'More time requested', 'More time requested',
        '<p class="govuk-body">The response deadline is now ${defaultRespondTime} on ${respondent1ResponseDeadlineEn}. There are {daysLeftToRespond} days remaining for you.' ||
        '<a href="{RESPONSE_TASK_LIST_URL}" rel="noopener noreferrer" class="govuk-link"> Respond to claim</a></p>',
        '<p class="govuk-body">The response deadline is now ${defaultRespondTime} on ${respondent1ResponseDeadlineCy}. There are {daysLeftToRespond} days remaining for you.' ||
        '<a href="{RESPONSE_TASK_LIST_URL}" rel="noopener noreferrer" class="govuk-link"> Respond to claim</a></p>',
'DEFENDANT');
