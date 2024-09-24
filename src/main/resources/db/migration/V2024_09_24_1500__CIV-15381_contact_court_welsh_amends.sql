
/**
 * Add task item template
 */
update dbs.task_item_template set task_name_cy = replace(task_name_cy, 'Contact the court to request a change to my case', 'Cysylltu â''r llys i ofyn i wneud newid i fy achos')
where task_name_cy like '%Contact the court to request a change to my case%';
