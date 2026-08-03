/**
 * Set all notifications to delete for ResponseTimeElapsed.Claimant scenario.
 */
UPDATE dbs.scenario
SET
  notifications_to_delete = '{"Notice.AAA6.DefResponse.MoreTimeRequested.Claimant",
                              "Notice.AAA6.ClaimIssue.Response.Await",
                              "Notice.AAA6.ClaimIssue.HWF.PhonePayment",
                              "Notice.AAA6.JR.Cancelled.Case.Stayed.Claimant"}'
WHERE
  name = 'Scenario.AAA6.DefResponse.ResponseTimeElapsed.Claimant';

/**
 * Set all notifications to delete for JudgmentsOnline.DefaultJudgmentIssued.Claimant scenario.
 */
UPDATE dbs.scenario
SET
  notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant",
                              "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
                              "Notice.AAA6.ClaimIssue.Response.Await",
                              "Notice.AAA6.ClaimIssue.Response.Required",
                              "Notice.AAA6.CP.Stay.Lifted.Claimant"}'
WHERE
  name = 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant';

/**
 * Set all notifications to delete for JudgmentsOnline.DefaultJudgmentIssued.Defendant scenario.
 */
UPDATE dbs.scenario
SET
  notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant",
                              "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant",
                              "Notice.AAA6.CP.Stay.Lifted.Defendant"}'
WHERE
  name = 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant';
