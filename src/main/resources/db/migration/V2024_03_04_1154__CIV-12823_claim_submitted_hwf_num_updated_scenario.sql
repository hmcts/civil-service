/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.Updated', '{"Notice.AAA6.ClaimIssue.HWF.InvalidRef"}', '{"Notice.AAA6.ClaimIssue.HWF.Updated": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.Updated', 'Your help with fees application has been updated', 'Mae eich cais am help i dalu ffioedd wedi cael ei ddiweddaru',
        '<p class="govuk-body">You''ve applied for help with the claim fee. You''ll receive an update from us within 5 to 10 working days.</p>',
        '<p class="govuk-body">Fe wnaethoch gais am help i dalu ffi’r hawliad. Byddwch yn cael diweddariad gennym mewn 5 i 10 diwrnod gwaith.</p>',
        'CLAIMANT');
