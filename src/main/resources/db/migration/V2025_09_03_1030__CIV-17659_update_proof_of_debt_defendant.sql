/**
 * Notifications to be deleted after proof of debt.
 */
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ProofofDebtPayment.Application.Defendant",
          "Notice.AAA6.ProofofDebtPayment.Application.Claimant",
          "Notice.AAA6.JudgmentsOnline.IssuedCCJ.Defendant",
          "Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant"}'
where name = 'Scenario.AAA6.ProofofDebtPayment.ApplicationProcessed.Defendant';

