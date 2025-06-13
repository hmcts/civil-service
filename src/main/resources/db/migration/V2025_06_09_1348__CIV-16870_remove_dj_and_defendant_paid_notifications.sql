/**
 * Notifications to be deleted after DJ journey until Claimant confirms payment.
 */
UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.Response.Required"}'
WHERE name = 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant';

UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant", "Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant"}'
WHERE name = 'Scenario.AAA6.ProofofDebtPayment.Application.Claimant';

/**
 * Add scenario to delete claimant notification
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ProofOfDebtPayment.Confirmation.Claimant',
        '{"Notice.AAA6.ProofofDebtPayment.Application.Claimant"}',
        '{"": []}');

/**
 * Add scenario to delete defendant notification
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ProofOfDebtPayment.Confirmation.Defendant',
        '{"Notice.AAA6.ProofofDebtPayment.Application.Defendant"}',
        '{"": []}');
