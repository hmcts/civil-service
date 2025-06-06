update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Await","Notice.AAA6.DefResponse.MoretimeRequested.Claimant","Notice.AAA6.ClaimIssue.HWF.PhonePayment"}'
where name = 'Scenario.AAA6.DefResponse.BilingualFlagSet.Claimant';
