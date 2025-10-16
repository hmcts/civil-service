/**
 * Add scenario
 * Delete old notification to update deadlines in new one
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Retrigger.ClaimIssue.Response.Await',
        '{"Notice.AAA6.ClaimIssue.Response.Await"}',
        '{"Notice.AAA6.ClaimIssue.Response.Await":["respondent1ResponseDeadlineEn", "respondent1ResponseDeadlineCy", "respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.Response.Await', 'Wait for defendant to respond', 'Aros i''r diffynnydd ymateb',
        '<p class="govuk-body">${respondent1PartyName} has until ${respondent1ResponseDeadlineEn} to respond. They can request an extra 28 days if they need it.</p>',
        '<p class="govuk-body">Mae gan ${respondent1PartyName} hyd at ${respondent1ResponseDeadlineCy} i ymateb. Gallant ofyn am 28 diwrnod ychwanegol os oes arnynt angen hynny.</p>',
        'CLAIMANT');
