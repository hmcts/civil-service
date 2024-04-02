/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.ResponseTimeElapsed.Claimant',
        '{"Notice.AAA6.DefResponse.MoretimeRequested.Claimant", "Notice.AAA6.ClaimIssue.Response.Await"}',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant',
        'Response to the claim', 'Response to the claim',
        '<p class="govuk-body">The defendant has not responded to the claim.</p>'
          '<p class="govuk-body">You can now request a county court judgment.<p/>'
          '<p class="govuk-body">The defendant can still respond to the claim before you ask for a judgment.</p>'
          '<p class="govuk-body"><a href="{COUNTY_COURT_JUDGEMENT_URL}" class="govuk-link">Request a CCJ</a></p>',
        '<p class="govuk-body">The defendant has not responded to the claim.</p>'
          '<p class="govuk-body">You can now request a county court judgment.<p/>'
          '<p class="govuk-body">The defendant can still respond to the claim before you ask for a judgment.</p>'
          '<p class="govuk-body"><a href="{COUNTY_COURT_JUDGEMENT_URL}" class="govuk-link">Request a CCJ</a></p>',
        'CLAIMANT');
