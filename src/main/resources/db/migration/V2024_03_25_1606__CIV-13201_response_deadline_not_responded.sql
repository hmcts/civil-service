/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.ResponseTimeElapsed.Claimant',
        '{"Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.ClaimIssue.Response.Await"}',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant" : ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant',
        'Response to the claim', 'Ymateb i’r hawliad',
        '<p class="govuk-body">${respondent1PartyName} has not responded to the claim.</p>'
          '<p class="govuk-body">You can now request a county court judgment.<p/>'
          '<p class="govuk-body">The defendant can still respond to the claim before you ask for a judgment.</p>'
          '<p class="govuk-body"><a href="{REQUEST_CCJ_URL}" class="govuk-link">Request a CCJ</a></p>',
        '<p class="govuk-body">Nid yw ${respondent1PartyName} wedi ymateb i’r hawliad.</p>'
          '<p class="govuk-body">Gallwch nawr wneud cais am ddyfarniad llys sirol.<p/>'
          '<p class="govuk-body">Gall y diffynnydd dal ymateb i’r hawliad cyn i chi ofyn am ddyfarniad.</p>'
          '<p class="govuk-body"><a href="{REQUEST_CCJ_URL}" class="govuk-link">Gwneud cais am CCJ</a></p>',
        'CLAIMANT');
