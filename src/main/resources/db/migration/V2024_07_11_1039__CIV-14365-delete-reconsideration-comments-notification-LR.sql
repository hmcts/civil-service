/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
/**
  Claimant/Defendant has commented the request for SDO reconsideration that opposing LR created in the first place
  Delete notification for claimant/defendant without notification for the LR
 */
VALUES ('Scenario.AAA6.CP.ReviewOrderRequestedbyRecipient.LegalRep',
        '{Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Claimant, Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Defendant}', '{}');
