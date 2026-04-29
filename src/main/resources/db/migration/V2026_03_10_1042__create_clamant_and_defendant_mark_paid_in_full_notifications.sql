UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Claimant","Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant"}',
                        notifications_to_create = '{"Notice.AAA6.JudgmentsOnline.PaidInFull.Claimant": []}'
WHERE name = 'Scenario.AAA6.ProofOfDebtPayment.Confirmation.Claimant';

UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Defendant","Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant"}',
                        notifications_to_create = '{"Notice.AAA6.JudgmentsOnline.PaidInFull.Defendant": []}'
WHERE name = 'Scenario.AAA6.ProofOfDebtPayment.Confirmation.Defendant';
