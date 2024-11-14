/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.PartRemission', '{"Notice.AAA6.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA6.ClaimIssue.HWF.PartRemission" : ["claimIssueRemissionAmount","claimIssueOutStandingAmount"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.PartRemission',
        'Your help with fees application has been reviewed',
        'Mae eich cais am help i dalu ffioedd wedi cael ei adolygu',
        '<p class="govuk-body">You''ll get help with the claim fee. ${claimIssueRemissionAmount} will be covered by fee remission. You must still pay the remaining fee of ${claimIssueOutStandingAmount}. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">Byddwch yn cael help gyda ffi’r hawliad. Bydd y swm o ${claimIssueRemissionAmount} yn cael ei ddileu. Bydd rhaid i chi dal dalu’r ffi sy’n weddill o ${claimIssueOutStandingAmount}. Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephoneWelshSpeaker}.</p>',
        'CLAIMANT');

