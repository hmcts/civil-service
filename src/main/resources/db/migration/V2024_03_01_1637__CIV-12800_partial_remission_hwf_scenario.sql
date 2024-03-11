/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.HWF.PartRemission', '{"Notice.AAA7.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA7.ClaimIssue.HWF.PartRemission" : ["claimIssueRemissionAmount","claimIssueOutStandingAmount"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA7.ClaimIssue.HWF.PartRemission',
        '<h2 class="govuk-notification-banner__title" id="govuk-notification-banner-title">Your help with fees application has been reviewed</h2>',
        '<h2 class="govuk-notification-banner__title" id="govuk-notification-banner-title">Your help with fees application has been reviewed</h2>',
        '<p class="govuk-body">You''ll get help with the claim fee. You''ll receive ${claimIssueRemissionAmount} towards it.</p> <p class="govuk-body">You must still pay the remaining fee of ${claimIssueOutStandingAmount}.You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">You''ll get help with the claim fee. You''ll receive ${claimIssueRemissionAmount} towards it.</p> <p class="govuk-body">You must still pay the remaining fee of ${claimIssueOutStandingAmount}.You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        'CLAIMANT');

