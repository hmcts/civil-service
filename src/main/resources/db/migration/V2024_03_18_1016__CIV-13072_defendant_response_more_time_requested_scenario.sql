/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.DefResponse.MoretimeRequested.Defendant', '{"Notice.AAA7.ClaimIssue.Response.Required"}',
        '{"Notice.AAA7.DefResponse.MoretimeRequested.Defendant" : ["respondent1ResponseDeadlineEn", "respondent1ResponseDeadlineCy","defaultRespondTime"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.DefResponse.MoretimeRequested.Defendant', 'More time requested', 'More time requested',
        '<p class="govuk-body">The response deadline for you is now ${defaultRespondTime} on ${respondent1ResponseDeadlineEn} ({daysLeftToRespond} days remaining).' ||
        '<a href=" " rel="noopener noreferrer" class="govuk-link"> Respond to claim</a></p>',
        '<p class="govuk-body">The response deadline for you is now ${defaultRespondTime} on ${respondent1ResponseDeadlineCy} ({daysLeftToRespond} days remaining).' ||
        '<a href=" " rel="noopener noreferrer" class="govuk-link"> Respond to claim</a></p>',
'DEFENDANT');
