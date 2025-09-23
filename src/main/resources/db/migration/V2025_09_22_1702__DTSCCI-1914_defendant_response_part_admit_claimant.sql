/**
 * Update scenario
 */
update
	dbs.scenario
set
	notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
								"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant",
								"Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant",
								"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
								"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant",
								"Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant",
								"Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant",
								"Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant",
								"Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant",
								"Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant"}'
where
	name = 'Scenario.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant';
