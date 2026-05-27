/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.MoreTimeRequested.JR.Cancelled.Claimant',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant",
        "Notice.AAA6.DefLip.Judgment.Requested.Claimant"}',
        '{"Notice.AAA6.DefResponse.MoreTimeRequested.JR.Cancelled.Claimant": []}');


/**
 * Update notification template
 */
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}'
where name = 'Scenario.AAA6.DefResponse.MoreTimeRequested.Defendant';
