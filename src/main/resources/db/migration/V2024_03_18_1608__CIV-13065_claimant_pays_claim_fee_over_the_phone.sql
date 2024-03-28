/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.PhonePayment',
        '{"Notice.AAA6.ClaimIssue.HWF.Requested", "Notice.AAA6.ClaimIssue.HWF.InvalidRef", "Notice.AAA6.ClaimIssue.HWF.InfoRequired", "Notice.AAA6.ClaimIssue.HWF.Updated", "Notice.AAA6.ClaimIssue.HWF.PartRemission", "Notice.AAA6.ClaimIssue.HWF.Rejected"}',
        '{"Notice.AAA6.ClaimIssue.HWF.PhonePayment": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.PhonePayment',
        'The claim fee has been paid',
        'The claim fee has been paid',
        '<p class="govuk-body">The claim fee has been paid in full.</p>',
        '<p class="govuk-body">The claim fee has been paid in full.</p>',
        'CLAIMANT');
