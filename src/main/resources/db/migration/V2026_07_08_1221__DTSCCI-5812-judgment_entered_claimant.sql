/**
 * Update scenario
 */
UPDATE dbs.scenario
SET
  notifications_to_delete = '{ "Notice.AAA6.DefLip.Judgment.Requested.Claimant"}'
WHERE
  name = 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentGranted.Claimant';
