/**
 * Update scenario
 */
UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant",
          "Notice.AAA6.ClaimantIntent.PartAdmit.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant",
          "Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant"}'
WHERE name = 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant';
