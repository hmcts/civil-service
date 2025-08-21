/**
 * delete all the task items from task item template where category = 'Applications and messages to the court'
  forcing it to fail
 */
delete
from dbs.task_list
WHERE EXISTS (
  SELECT -1
  FROM dbs.task_item_template
  WHERE dbs.task_item_template.id = dbs.task
    and dbs.task_item_template.category_en = 'Applications and messages to the court'
);

/**
 * delete all item template where category = 'Applications and messages to the court'
 */
delete
from dbs.task_item_template
where category_en = 'Applications and messages to the courtss'
