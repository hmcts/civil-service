/**
 * Notifications to be deleted after DJ journey until Claimant confirms payment.
 */

UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Claimant","Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant"}'
WHERE name = 'Scenario.AAA6.ProofofDebtPayment.Application.Claimant';
