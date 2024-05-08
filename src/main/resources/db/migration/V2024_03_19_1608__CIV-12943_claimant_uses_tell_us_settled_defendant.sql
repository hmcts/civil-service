/**
 * Add scenario
 */
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
          "Notice.AAA6.DefResponse.Full DefenceOrPartAdmin.AlreadyPaid.Defendant",
          "Notice.AAA6.DefResponse.Full Defence. FullDispute.RefusedMediation.Defendant",
          "Notice.AAA6.DefResponse.Full Defence. FullDispute.SuggestedMediation.Defendant",
          "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant",
          "Notice.AAA6.CP.Hearing.Scheduled",
          "Notice.AAA6.CP.HearingDocuments.OtherPartyUploaded",
          "Notice.AAA6.CP.HearingDocuments.Upload",
          "Notice.AAA6.CP.OrderMade.Completed",
          "Notice.AAA6.CP.Bundle.Ready",
          "Notice.AAA6.CP.Trial Arrangements.Finalised",
          "Notice.AAA6.CP.Trial Arrangements.Required"
        }',
        '{"Notice.AAA6.ClaimantIntent.ClaimSettleEvent.Defendant": ["applicant1ClaimSettledDateEn", "applicant1ClaimSettledDateCy","applicant1PartyName"]}');
/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimSettleEvent.Defendant',
        'The claim is settled',
        'Mae’r hawliad wedi’i setlo',
        '<p class="govuk-body">${applicant1PartyName} has confirmed that you settled on ${applicant1ClaimSettledDateEn}.</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} wedi cadarnhau eich bod wedi talu ar ${applicant1ClaimSettledDateCy}.</p>',
        'DEFENDANT');
