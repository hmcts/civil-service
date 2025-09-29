/**
 * Notifications to be deleted after defendant response.
 */
UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Claimant","Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant","Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant"}'
WHERE name = 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant';



UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant","Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant"}'
WHERE name = 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant';
