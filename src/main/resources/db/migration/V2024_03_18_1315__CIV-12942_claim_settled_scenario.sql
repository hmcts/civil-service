/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimSettledEvent.Claimant',
        '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.Requested", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.ClaimIssue.HWF.PhonePayment",
        "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant", "Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant",
        "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant",
        "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant",
        "Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant",
        "Notice.AAA6.DefResponse.Full Defence. FullDispute.SuggestedMediation.Claimant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimantIntent.PartAdmit.Claimant",
        "Notice.AAA6.ClaimantIntent.FullAdmit.Claimant", "Notice.AAA6.ClaimantIntent.Mediation.Claimant", "Notice.AAA6.ClaimantIntent.GoToHearing.Claimant",
        "Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant", "Notice.AAA6.ClaimantIntent.SettlementNoResponse.Claimant",
        "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantAccepted.Claimant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant",
        "Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant", "Notice.AAA6.ClaimantIntent.Mediation.CARM.Claimant", "Notice.AAA6.MediationSuccessful.CARM.Claimant",
        "Notice.AAA6.MediationUnsuccessful.NOTClaimant1NonContactable.CARM.Claimant", "Notice.AAA6.MediationUnsuccessful.Claimant1NonAttendance.CARM.Claimant",
        "Notice.AAA6.MediationUnsuccessful.TrackChange.CARM.Claimant",
        "Notice.AAA6.ClaimantIntent.RequestedCCJ.Claimant", "Notice.AAA6.ClaimantIntent.Defendant.OrgLtdCo.Claimant", "Notice.AAA6.CP.Hearing.Scheduled", "Notice.AAA6.CP.Trial Arrangements.Required",
        "Notice.AAA6.CP.Bundle.Ready", "Notice.AAA6.CP.OrderMade.Completed", "Notice.AAA6.CP.HearingDocuments.Upload", "Notice.AAA6.CP.HearingDocuments.OtherPartyUploaded", "Notice.AAA6.CP.HearingFee.Paid",
        "Notice.AAA6.CP.HearingFee.Required", "Notice.AAA6.CP.HearingFee.HWF.AppliedFor", "Notice.AAA6.CP.HearingFee.HWF.Rejected", "Notice.AAA6.CP.HearingFee.HWF.PartRemission",
        "Notice.AAA6.CP.HearingFee.HWF.FullRemission", "Notice.AAA6.CP.HearingFee.HWF.InfoRequired", "Notice.AAA6.CP.HearingFee.HWF.InvalidRef", "Notice.AAA6.CP.HearingFee.HWF.ReviewUpdate"}',
        '{"Notice.AAA6.ClaimantIntent.ClaimSettledEvent.Claimant" : ["applicant1ClaimSettledDateEn", "applicant1ClaimSettledDateCy" ,"respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimSettledEvent.Claimant', 'The claim is settled', 'Mae’r hawliad wedi’i setlo',
        '<p class="govuk-body">You have confirmed that the claim against ${respondent1PartyName} was settled on ${applicant1ClaimSettledDateEn}.</p>'
          '<p class="govuk-body">The defendant has 19 days from the date of settlement to notify the court of any objection.</p>',
        '<p class="govuk-body">You have confirmed that the claim against ${respondent1PartyName} was settled on ${applicant1ClaimSettledDateEn}.</p>'
          '<p class="govuk-body">The defendant has 19 days from the date of settlement to notify the court of any objection.</p>',
        'CLAIMANT')
