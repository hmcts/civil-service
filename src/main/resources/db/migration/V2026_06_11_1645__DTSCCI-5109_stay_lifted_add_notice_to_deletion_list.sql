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
