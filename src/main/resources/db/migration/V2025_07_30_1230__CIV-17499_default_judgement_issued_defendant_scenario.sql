/**
 * Update notification template
 */
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant",
          "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant"}'
where name = 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant';
