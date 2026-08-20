/**
 * Update scenario
 */
UPDATE dbs.scenario
SET
  notifications_to_delete = '{ "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant",
                               "Notice.AAA6.ClaimIssue.Response.Required",
                               "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant" }'
WHERE
  name = 'Scenario.AAA6.Discontinue.NoticeOfDiscontinuanceIssued.Defendant';
