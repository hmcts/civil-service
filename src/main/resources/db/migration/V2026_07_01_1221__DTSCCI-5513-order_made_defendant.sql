/**
 * Update scenario
 */
UPDATE dbs.scenario
SET
  notifications_to_delete = '{ "Notice.AAA6.CP.SDOMadebyLA.Defendant"}'
WHERE
  name = 'Scenario.AAA6.CP.OrderMade.Defendant';
