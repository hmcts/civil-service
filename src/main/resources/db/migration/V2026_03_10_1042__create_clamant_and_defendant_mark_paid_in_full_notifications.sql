UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Claimant"}',
                        notifications_to_create = '{"Notice.AAA6.JudgmentsOnline.PaidInFull.Claimant": []}'
WHERE name = 'Scenario.AAA6.ProofOfDebtPayment.Confirmation.Claimant';

UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Defendant"}',
                        notifications_to_create = '{"Notice.AAA6.JudgmentsOnline.PaidInFull.Defendant": []}'
WHERE name = 'Scenario.AAA6.ProofOfDebtPayment.Confirmation.Defendant';
