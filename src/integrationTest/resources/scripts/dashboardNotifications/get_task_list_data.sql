INSERT INTO dbs.task_item_template
(id, task_name_en, hint_text_en, category_en, task_name_cy, hint_text_cy, category_cy, "name", task_status_sequence, "role", task_order, created_at)
VALUES(12255, 'task_name_en', 'hint_text_en', 'category_en', 'task_name_cy', 'hint_text_cy', 'category_cy', 'name', array[1,2,3,4,5], 'defendant', 0, now());


INSERT INTO dbs.task_list
(id, task_item_template_id, reference, current_status, next_status, task_name_en, hint_text_en, task_name_cy, hint_text_cy, message_params, created_at, updated_at, updated_by)
VALUES('8c2712da-47ce-4050-bbee-650134a7b9e5', 12255, '123', 0, 1, 'task_name_en', 'hint_text_en', 'task_name_cy', 'hint_text_cy', null, null, null, 'Test');
