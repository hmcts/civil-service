/**
 * Notifications to be deleted after defendant response.
 */
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Claimant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.ClaimantIntent.FullAdmit.Claimant"}'
where name = 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant';
