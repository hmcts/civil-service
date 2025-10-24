/**
 * Update scenario
 */
UPDATE dbs.scenario
SET
  notifications_to_delete = '{ "Notice.AAA6.ClaimIssue.Response.Await",
                               "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant",
                               "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant",
                               "Notice.AAA6.ClaimIssue.HWF.PhonePayment",
                               "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant",
                               "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant" }'
WHERE
  name = 'Scenario.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant';
