/**
 * Delete task list items
 */
DELETE FROM dbs.task_item_template
WHERE (template_name = 'Hearing.Fee.Pay' AND scenario_name = 'Scenario.AAA6.CaseProceedsInCaseman.Claimant.FastTrack' AND task_order = 9)
   OR (template_name = 'Hearing.Document.Upload' AND scenario_name = 'Scenario.AAA6.CaseProceedsInCaseman.Claimant.FastTrack' AND task_order = 10)
   OR (template_name = 'Hearing.Arrangements.Add' AND scenario_name = 'Scenario.AAA6.CaseProceedsInCaseman.Claimant.FastTrack' AND task_order = 11)
   OR (template_name = 'Hearing.Document.Upload' AND scenario_name = 'Scenario.AAA6.CaseProceedsInCaseman.Defendant.FastTrack' AND task_order = 10)
   OR (template_name = 'Hearing.Arrangements.Add' AND scenario_name = 'Scenario.AAA6.CaseProceedsInCaseman.Defendant.FastTrack' AND task_order = 11);
