UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.ProofofDebtPayment.Application.Claimant","Notice.AAA6.JudgmentsOnline.IssuedCCJ.Claimant"}'
WHERE name = 'Scenario.AAA6.ProofOfDebtPayment.Confirmation.Claimant';

UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.ProofofDebtPayment.Application.Defendant", "Notice.AAA6.JudgmentsOnline.IssuedCCJ.Defendant"}'
WHERE name = 'Scenario.AAA6.ProofOfDebtPayment.Confirmation.Defendant';

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ProofOfDebtPayment.Confirmation.Defendant',
        '{}',
        '{"Notice.AAA6.ProofofDebtPayment.ApplicationProcessed.Defendant": []}');
