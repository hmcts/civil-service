/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.Response.Required',
  '{"Notice.AAA6.ClaimIssue.Response.Required"}',
  '{"Notice.AAA6.ClaimIssue.Response.Required" : ["ccdCaseReference", "defaultRespondTime", "respondent1ResponseDeadlineEn", "respondent1ResponseDeadlineCy", "daysLeftToRespond"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.Response.Required', 'You haven''t responded to the claim', 'Nid ydych wedi ymateb i''r hawliad',
        '<p class="govuk-body">You need to respond before ${defaultRespondTime} on ${respondent1ResponseDeadlineEn}. There are {daysLeftToRespond} days remaining.</p><p class="govuk-body"><a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">Respond to the claim</a></p>',
        '<p class="govuk-body">Mae angen i chi ymateb cyn ${defaultRespondTime} ar ${respondent1ResponseDeadlineCy}. Mae yna {daysLeftToRespond} diwrnod yn weddill.</p><p class="govuk-body"><a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">Ymateb i''r hawliad</a></p>',
        'DEFENDANT');
