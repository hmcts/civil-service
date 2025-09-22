
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Claimant", "Notice.AAA6.CP.SDOMadebyLA.Claimant", "Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Claimant", "Notice.AAA6.ClaimantIntent.GoToHearing.Claimant", "Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Claimant", "Notice.AAA6.ClaimIssue.Response.Await"}'
                    where name = 'Scenario.AAA6.CP.OrderMade.Claimant';

update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Defendant", "Notice.AAA6.CP.SDOMadebyLA.Defendant", "Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Defendant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant",
          "Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant"}'
where name = 'Scenario.AAA6.CP.OrderMade.Defendant';
