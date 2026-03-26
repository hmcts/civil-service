/**
 * Add scenario to delete claimant notification
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MarkPaidInFull.Confirmation.Claimant',
        '{
                              "Notice.AAA6.ProofofDebtPayment.Application.Claimant",
                              "Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant",
                              "Notice.AAA6.JudgmentsOnline.IssuedCCJ.Claimant"
                            }',
        '{"Notice.AAA6.JudgmentsOnline.PaidInFull.Claimant": []}');

/**
 * Add scenario to delete defendant notification
 */

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MarkPaidInFull.Confirmation.Defendant',
        '{
                              "Notice.AAA6.ProofofDebtPayment.Application.Defendant",
                              "Notice.AAA6.JudgmentsOnline.IssuedCCJ.Defendant",
                              "Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant"
                            }',
        '{"Notice.AAA6.ProofofDebtPayment.ApplicationProcessed.Defendant": []}');


