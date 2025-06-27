/**
 * Notifications to be deleted after defendant response.
 */
UPDATE dbs.scenario
SET notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
        "Notice.AAA6.ClaimIssue.Response.Await",
        "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoretimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.FullRemission",
        "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant",
        "Notice.AAA6.DefResponse.Full Defence.FullDispute.SuggestedMediation.Claimant",
        "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant","Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
WHERE name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant';
