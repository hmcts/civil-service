-- PRE DEPLOYMENT DATA PREPARATION STEPS
-----------------------------------------
select task_id from cft_task_db.tasks where role_category is null and jurisdiction = 'CIVIL' and task_name in ('Request for transfer online case');
--  take a backup of all the task_id's

select count(*), task_name  from cft_task_db.tasks where role_category is null and jurisdiction = 'CIVIL' and task_name in ('Request for transfer online case') group by task_name ;
--  97	  Request for transfer online case


-- IMPLEMENTATION STEPS
-----------------------------

update cft_task_db.tasks set role_category = 'ADMIN' where role_category is null and jurisdiction = 'CIVIL' and task_name in ('Request for transfer online case');


-- POST IMPLEMENTATION VERIFICATION STEPS
--------------------------------------------

select count(*) from cft_task_db.tasks where role_category is null and jurisdiction = 'CIVIL' and task_name in ('Request for transfer online case'); 
--  0
