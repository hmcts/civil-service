BEGIN;

update cft_task_db.tasks set role_category = 'ADMIN' where role_category is null and jurisdiction = 'CIVIL' and task_name in ('Request for transfer online case');

COMMIT;





