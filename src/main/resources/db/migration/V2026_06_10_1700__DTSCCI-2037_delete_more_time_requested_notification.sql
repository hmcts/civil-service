update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant", "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}'
where name = 'Scenario.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}'
where name = 'Scenario.AAA6.DefResponse.ResponseTimeElapsed.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}'
where name = 'Scenario.AAA6.DefResponse.ResponseTimeElapsed.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant", "Notice.AAA6.DefResponse.Full Defence.FullDispute.SuggestedMediation.Defendant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.CARM.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}'
where name = 'Scenario.AAA6.DefResponse.BilingualFlagSet.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant", "Notice.AAA6.DefResponse.Full Defence.FullDispute.SuggestedMediation.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}'
where name = 'Scenario.AAA6.DefResponse.BilingualFlagSet.WelshEnabled.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant';



