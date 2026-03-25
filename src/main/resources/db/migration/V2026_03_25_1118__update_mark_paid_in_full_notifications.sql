UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.ProofofDebtPayment.Application.Claimant"}'
WHERE name = 'Scenario.AAA6.ProofOfDebtPayment.Confirmation.Claimant';

UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.ProofofDebtPayment.Application.Defendant"}',
                        notifications_to_create = '{"Notice.AAA6.ProofofDebtPayment.ApplicationProcessed.Defendant": []}'
WHERE name = 'Scenario.AAA6.ProofOfDebtPayment.Confirmation.Defendant';
