/**
 * Update notification template
 */
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant","Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}'
where name = 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant';
