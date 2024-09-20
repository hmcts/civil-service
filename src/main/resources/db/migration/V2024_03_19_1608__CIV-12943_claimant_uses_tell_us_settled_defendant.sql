/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimSettleEvent.Defendant',
        '{
          "Notice.AAA6.ClaimantIntent.PartAdmit.Defendant",
          "Notice.AAA6.ClaimantIntent.Mediation.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant",
          "Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant",
          "Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant",
          "Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant",
          "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantAccepted.Defendant",
          "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan. Defendant",
          "Notice.AAA6.ClaimIssue.Response.Required",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant",
          "Notice.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant",
          "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant",
          "Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Defendant",
          "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant",
          "Notice.AAA6.CP.Hearing.Scheduled",
          "Notice.AAA6.CP.HearingDocuments.OtherPartyUploaded",
          "Notice.AAA6.CP.HearingDocuments.Upload",
          "Notice.AAA6.CP.OrderMade.Completed",
          "Notice.AAA6.CP.Bundle.Ready",
          "Notice.AAA6.CP.Trial Arrangements.Finalised",
          "Notice.AAA6.CP.Trial Arrangements.Required",
          "Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Defendant",
          "Notice.AAA6.ClaimantIntent.Mediation.CARM.Defendant",
          "Notice.AAA6.MediationSuccessful.CARM.Defendant",
          "Notice.AAA6.MediationUnsuccessful.NOTDefendant1NonContactable.CARM.Defendant",
          "Notice.AAA6.MediationUnsuccessful.Defendant1NonAttendance.CARM.Defendant",
          "Notice.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant"
        }',
        '{"Notice.AAA6.ClaimantIntent.ClaimSettleEvent.Defendant": ["applicant1ClaimSettledDateEn", "applicant1ClaimSettledDateCy"]}');
/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimSettleEvent.Defendant',
        'The claim is settled',
        'Mae’r hawliad wedi’i setlo',
        '<p class="govuk-body">The claimant has confirmed that this case was settled on ${applicant1ClaimSettledDateEn}.</p>'
          '<p class="govuk-body">If you do not agree that the case is settled, please outline your objections in writing within 19 days of the settlement date, to the Civil National Business Centre using the email address at {cmcCourtEmailId}</p>',
        '<p class="govuk-body">The claimant has confirmed that this case was settled on ${applicant1ClaimSettledDateEn}.</p>'
          '<p class="govuk-body">If you do not agree that the case is settled, please outline your objections in writing within 19 days of the settlement date, to the Civil National Business Centre using the email address at {cmcCourtEmailId}</p>',
        'DEFENDANT');
