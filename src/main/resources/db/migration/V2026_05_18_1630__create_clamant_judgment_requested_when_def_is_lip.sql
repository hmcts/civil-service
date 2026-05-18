/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefIsLip.Judgment.Requested.Claimant',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant"}',
        '{"Notice.AAA6.DefLip.Judgment.Requested.Claimant": []}');
