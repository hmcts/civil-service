INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.DefaultJudgmentEntered.Claimant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant",
          "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
          "Notice.AAA6.ClaimIssue.Response.Await",
          "Notice.AAA6.ClaimIssue.Response.Required",
          "Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant"}',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentEntered.Claimant": ["djClaimantNotificationMessage", "djClaimantNotificationMessageCy"]}');

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
SELECT task_name_en,
       category_en,
       task_name_cy,
       category_cy,
       template_name,
       'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentEntered.Claimant',
       task_status_sequence,
       role,
       task_order
FROM dbs.task_item_template
WHERE scenario_name = 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant';

UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant",
          "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
          "Notice.AAA6.ClaimIssue.Response.Await",
          "Notice.AAA6.ClaimIssue.Response.Required",
          "Notice.AAA6.JudgmentsOnline.DefaultJudgmentEntered.Claimant"}'
WHERE name = 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant';

UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant",
          "Notice.AAA6.JudgmentsOnline.DefaultJudgmentEntered.Claimant",
          "Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant"}'
WHERE name = 'Scenario.AAA6.ProofofDebtPayment.Application.Claimant';

UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant",
          "Notice.AAA6.JudgmentsOnline.DefaultJudgmentEntered.Claimant"}'
WHERE name = 'Scenario.AAA6.JudgmentsOnline.SetAsideError.Claimant';

UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Claimant",
          "Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant",
          "Notice.AAA6.JudgmentsOnline.DefaultJudgmentEntered.Claimant"}'
WHERE name = 'Scenario.AAA6.ProofOfDebtPayment.Confirmation.Claimant';
