update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment","Notice.AAA6.DefResponse.MoreTimeRequested.Claimant","Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant", "Notice.AAA6.DefResponse.Full Defence.FullDispute.SuggestedMediation.Defendant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.CARM.Defendant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant", "Notice.AAA6.DefResponse.Full Defence.FullDispute.SuggestedMediation.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}'
where name = 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefendantNoticeOfChange.ClaimRemainsOnline.Claimant", "Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant", "Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant", "Notice.AAA6.ClaimantIntent.SettlementNoResponse.Claimant", "Notice.AAA6.ClaimantIntent.Settlement.DefendantResponseAccepts.Claimant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.FullRemission"}'
where name = 'Scenario.AAA6.DefendantNoticeOfChange.ClaimMovesOffline.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Await","Notice.AAA6.DefResponse.MoreTimeRequested.Claimant","Notice.AAA6.ClaimIssue.HWF.PhonePayment"}'
where name = 'Scenario.AAA6.DefResponse.BilingualFlagSet.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Await","Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant","Notice.AAA6.ClaimIssue.HWF.PhonePayment","Notice.AAA6.DefResponse.MoreTimeRequested.Claimant","Notice.AAA6.ClaimIssue.HWF.FullRemission"}'
where name = 'Scenario.Notice.AAA6.DefLRResponse.FullDefence.Counterclaim.Claimant';
