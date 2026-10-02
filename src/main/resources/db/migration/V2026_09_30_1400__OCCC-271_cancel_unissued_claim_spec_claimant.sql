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
