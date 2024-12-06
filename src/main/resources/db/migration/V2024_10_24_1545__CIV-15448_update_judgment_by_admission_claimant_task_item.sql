/**
* Delete task list references
*/
DELETE FROM dbs.task_list
WHERE task_item_template_id IN (
  SELECT id FROM dbs.task_item_template
            WHERE (template_name = 'Judgment.Cosc' AND
                   scenario_name = 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant')
  );

/**
 * Delete task item
 */
DELETE FROM dbs.task_item_template WHERE scenario_name = 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant'
  AND template_name = 'Judgment.Cosc';
