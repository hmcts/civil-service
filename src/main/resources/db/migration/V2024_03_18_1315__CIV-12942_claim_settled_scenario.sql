/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.ClaimSettledEvent.Claimant',
        '{"Notice.AAA7.ClaimIssue.Response.Await", "Notice.AAA7.ClaimIssue.HWF.Requested", "Notice.AAA7.ClaimIssue.HWF.FullRemission", "Notice.AAA7.ClaimIssue.HWF.PhonePayment",
        "Notice.AAA7.DefResponse.MoretimeRequested.Claimant", "Notice.AAA7.DefResponse.FullAdmit.PayImmediately.Claimant", "Notice.AAA7.DefResponse.PartAdmit.PayImmediately.Claimant",
        "Notice.AAA7.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA7.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant",
        "Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant",
        "Notice.AAA7.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA7.DefResponse.Full Defence.AlreadyPaid.Claimant", "Notice.AAA7.DefResponse.Full Defence. FullDispute.RefusedMediation.Claimant",
        "Notice.AAA7.DefResponse.Full Defence. FullDispute.SuggestedMediation.Claimant", "Notice.AAA7.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA7.ClaimantIntent.PartAdmit.Claimant",
        "Notice.AAA7.ClaimantIntent.FullAdmit.Claimant", "Notice.AAA7.ClaimantIntent.Mediation.Claimant", "Notice.AAA7.ClaimantIntent.GoToHearing.Claimant",
        "Notice.AAA7.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant", "Notice.AAA7.ClaimantIntent.SettlementAgreement.NoDefendantResponse.Claimant",
        "Notice.AAA7.ClaimantIntent.SettlementAgreement.DefendantAccepted.Claimant", "Notice.AAA7.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant",
        "Notice.AAA7.ClaimantIntent.RequestedCCJ.Claimant", "Notice.AAA7.ClaimantIntent.Defendant.OrgLtdCo.Claimant", "Notice.AAA7.CP.Hearing.Scheduled", "Notice.AAA7.CP.Trial Arrangements.Required",
        "Notice.AAA7.CP.Bundle.Ready", "Notice.AAA7.CP.OrderMade.Completed", "Notice.AAA7.CP.HearingDocuments.Upload", "Notice.AAA7.CP.HearingDocuments.OtherPartyUploaded", "Notice.AAA7.CP.HearingFee.Paid",
        "Notice.AAA7.CP.HearingFee.Required", "Notice.AAA7.CP.HearingFee.HWF.AppliedFor", "Notice.AAA7.CP.HearingFee.HWF.Rejected", "Notice.AAA7.CP.HearingFee.HWF.PartRemission",
        "Notice.AAA7.CP.HearingFee.HWF.FullRemission", "Notice.AAA7.CP.HearingFee.HWF.InfoRequired", "Notice.AAA7.CP.HearingFee.HWF.InvalidRef", "Notice.AAA7.CP.HearingFee.HWF.ReviewUpdate"}',
        '{"Notice.AAA7.ClaimantIntent.ClaimSettledEvent.Claimant" : ["applicant1ClaimSettledDateEn", "applicant1ClaimSettledDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.ClaimSettledEvent.Claimant', 'The claim is settled' , 'The claim is settled',
        '<p class="govuk-body">You have confirmed that the defendant paid you on ${applicant1ClaimSettledDateEn}.</P>',
        '<p class="govuk-body">You have confirmed that the defendant paid you on ${applicant1ClaimSettledDateCy}.</P>',
        'CLAIMANT')
