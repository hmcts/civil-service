UPDATE dbs.task_item_template SET
  task_name_cy = '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link"><a>Gweld yr ymateb i''r hawliad</a>',
  category_cy = 'Yr ymateb'
WHERE scenario_name = 'Scenario.AAA6.DefResponse.BilingualFlagSet.Claimant';

DELETE FROM dbs.task_item_template WHERE scenario_name = 'Scenario.AAA6.EnglishDefResponse.BilingualFlagSet.Claimant';

/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.BilingualFlagSet.WelshEnabled.Claimant',
        '{"Notice.AAA6.ClaimIssue.Response.Await",
          "Notice.AAA6.DefResponse.MoretimeRequested.Claimant",
          "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
          "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}',
        '{"Notice.AAA6.DefResponse.BilingualFlagSet.Claimant": []}');
