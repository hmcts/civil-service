/**
 * Add scenario
 */
/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.ClaimSettleEvent.Defendant',
        '{
          "Notice.AAA7.ClaimantIntent.PartAdmit.Defendant",
          "Notice.AAA7.ClaimantIntent.Mediation.Defendant",
          "Notice.AAA7.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant",
          "Notice.AAA7.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant",
          "Notice.AAA7.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant",
          "Notice.AAA7.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant",
          "Notice.AAA7.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant",
          "Notice.AAA7.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant",
          "Notice.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant",
          "Notice.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant",
          "Notice.AAA7.ClaimantIntent.SettlementAgreement.DefendantAccepted.Defendant",
          "Notice.AAA7.ClaimantIntent.SettlementAgreement.DefendantRejected.Defendant",
          "Notice.AAA7.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan. Defendant",
          "Notice.AAA7.ClaimIssue.Response.Required",
          "Notice.AAA7.DefResponse.FullOrPartAdmit.PayImmediately.Defendant",
          "Notice.AAA7.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA7.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant",
          "Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant",
          "Notice.AAA7.DefResponse.Full DefenceOrPartAdmin.AlreadyPaid.Defendant",
          "Notice.AAA7.DefResponse.Full Defence. FullDispute.RefusedMediation.Defendant",
          "Notice.AAA7.DefResponse.Full Defence. FullDispute.SuggestedMediation.Defendant",
          "Notice.AAA7.DefResponse.ResponseTimeElapsed.Defendant",
          "Notice.AAA7.CP.Hearing.Scheduled",
          "Notice.AAA7.CP.HearingDocuments.OtherPartyUploaded",
          "Notice.AAA7.CP.HearingDocuments.Upload",
          "Notice.AAA7.CP.OrderMade.Completed",
          "Notice.AAA7.CP.Bundle.Ready",
          "Notice.AAA7.CP.Trial Arrangements.Finalised",
          "Notice.AAA7.CP.Trial Arrangements.Required"
        }',
        '{"Notice.AAA7.ClaimantIntent.ClaimSettleEvent.Defendant": ["applicant1ClaimSettledDateEn", "applicant1ClaimSettledDateCy"]}');
/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.ClaimSettleEvent.Defendant',
        'The claim is settled',
        'The claim is settled',
        '<p class="govuk-body">The claimant has confirmed that you settled on ${applicant1ClaimSettledDateEn}.</p>',
        '<p class="govuk-body">The claimant has confirmed that you settled on ${applicant1ClaimSettledDateCy}.</p>',
        'DEFENDANT');
