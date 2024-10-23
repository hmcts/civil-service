
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Claimant", "Notice.AAA6.CP.SDOMadebyLA.Claimant", "Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Claimant", ""}'
                    where name = 'Scenario.AAA6.CP.OrderMade.Claimant';

update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Defendant", "Notice.AAA6.CP.SDOMadebyLA.Defendant", "Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Defendant"}'
where name = 'Scenario.AAA6.CP.OrderMade.Defendant';
