/**
 * Notifications to be deleted after claimant response.
 */
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Claimant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant",
          "Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant",
          "Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant",
          "Notice.AAA6.ClaimantIntent.FullAdmit.Claimant"}'
where name = 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant';


update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant",
          "Notice.AAA6.ClaimantIntent.PartAdmit.Defendant"}'
where name = 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant';
