/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.ResponseTimeElapsed.Defendant',
        '{"Notice.AAA6.DefResponse.MoretimeRequested.Defendant", "Notice.AAA6.ClaimIssue.Response.Required"}',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant',
        'Response to the claim', 'Response to the claim',
        '<p class="govuk-body">You have not responded to the claim.</p>'
        '<p class="govuk-body">The claimant can now request a county court judgment.<p/>'
        '<p class="govuk-body">You can still respond to the claim before they ask for a judgment.</p>'
        '<p class="govuk-body">A County Court Judgment can mean you find it difficult to get credit, like a mortgage or mobile phone contact. Bailiffs could also be sent to your home.</p>'
        '<p class="govuk-body"><a href="{RESPONSE_TASK_LIST_URL}" class="govuk-link">Respond to claim</a></p>',
        '<p class="govuk-body">You have not responded to the claim.</p>'
        '<p class="govuk-body">The claimant can now request a county court judgment.<p/>'
        '<p class="govuk-body">You can still respond to the claim before they ask for a judgment.</p>'
        '<p class="govuk-body">A County Court Judgment can mean you find it difficult to get credit, like a mortgage or mobile phone contact. Bailiffs could also be sent to your home.</p>'
        '<p class="govuk-body"><a href="{RESPONSE_TASK_LIST_URL}" class="govuk-link">Respond to claim</a></p>',
        'DEFENDANT');
