/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CancelUnissuedClaimSpec.Claimant',
        '{"Notice.AAA6.ClaimIssue.ClaimSubmit.Required", "Notice.AAA6.ClaimIssue.ClaimFee.Required", "Notice.AAA6.ClaimIssue.HWF.Requested",
        "Notice.AAA6.ClaimIssue.HWF.InfoRequired", "Notice.AAA6.ClaimIssue.HWF.InvalidRef", "Notice.AAA6.ClaimIssue.HWF.Updated",
        "Notice.AAA6.ClaimIssue.HWF.PartRemission", "Notice.AAA6.ClaimIssue.HWF.Rejected", "Notice.AAA6.ClaimIssue.HWF.FullRemission",
        "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}',
        '{"Notice.AAA6.CancelUnissuedClaimSpec.Claimant" : ["respondent1PartyName", "cancelUnissuedClaimSpecDateEn", "cancelUnissuedClaimSpecDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CancelUnissuedClaimSpec.Claimant', 'Claim is unissued and cancelled', 'Nid yw’r hawliad wedi ei gyflwyno ac mae wedi''i ganslo',
        '<p class="govuk-body">The unissued claim against ${respondent1PartyName} was cancelled on ${cancelUnissuedClaimSpecDateEn}.</p>',
        '<p class="govuk-body">The unissued claim against ${respondent1PartyName} was cancelled on ${cancelUnissuedClaimSpecDateCy}.</p>',
        'CLAIMANT');
