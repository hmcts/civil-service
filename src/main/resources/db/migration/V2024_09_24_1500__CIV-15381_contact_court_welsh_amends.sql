
/**
 * Add task item template
 */
update dbs.task_item_template set task_name_cy = replace(task_name_cy, 'Contact the court to request a change to my case', 'Cysylltu â''r llys i ofyn i wneud newid i fy achos')
where scenario_name in ('Scenario.AAA6.ClaimIssue.ClaimSubmit.Required','Scenario.AAA6.ClaimIssue.ClaimFee.Required','Scenario.AAA6.ClaimIssue.Response.Required','Scenario.AAA6.ClaimIssue.Response.Await','Scenario.AAA6.ClaimIssue.HWF.Requested','Scenario.AAA6.DefendantNoticeOfChange.Claimant')
and template_name = 'Application.Create';
