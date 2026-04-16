UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant"}',
                        notifications_to_create = '{}'
WHERE name = 'Notice.AAA6.ProofofDebtPayment.Application.Claimant';
