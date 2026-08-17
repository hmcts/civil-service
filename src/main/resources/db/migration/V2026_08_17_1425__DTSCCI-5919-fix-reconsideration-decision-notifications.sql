/**
 * Update notifications_to_delete for Scenario.AAA6.CP.ReconDecisionMade.Claimant
 */
UPDATE dbs.scenario
SET
  notifications_to_delete = '{"Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Claimant", "Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Claimant"}'
WHERE
  name = 'Scenario.AAA6.CP.ReconDecisionMade.Claimant';

/**
 * Update notifications_to_delete for Scenario.AAA6.CP.ReconDecisionMade.Defendant
 */
UPDATE dbs.scenario
SET
  notifications_to_delete = '{"Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Defendant", "Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Defendant"}'
WHERE
  name = 'Scenario.AAA6.CP.ReconDecisionMade.Defendant';
