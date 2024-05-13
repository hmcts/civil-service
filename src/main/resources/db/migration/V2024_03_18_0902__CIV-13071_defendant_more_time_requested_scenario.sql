/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.MoreTimeRequested.Claimant', '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}', '{"Notice.AAA6.DefResponse.MoreTimeRequested.Claimant" : ["defaultRespondTime", "respondent1ResponseDeadlineEn", "respondent1ResponseDeadlineCy", "daysLeftToRespond"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.MoreTimeRequested.Claimant', 'More time requested', 'Cais am fwy o amser',
        '<p class="govuk-body">The response deadline for the defendant is now ${defaultRespondTime} on ${respondent1ResponseDeadlineEn}. There are {daysLeftToRespond} days remaining.</p>',
        '<p class="govuk-body">Y terfyn amser ar gyfer ymateb y diffynnydd nawr yw ${defaultRespondTime} ar ${respondent1ResponseDeadlineCy}. Mae yna {daysLeftToRespond} diwrnod yn weddill.</p>',
        'CLAIMANT');

