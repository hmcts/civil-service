create index idx_scenario_name on dbs.scenario(name);

create index idx_dashboard_notifications_templates_template_name on dbs.dashboard_notifications_templates(template_name);
create index idx_dashboard_notifications_reference_citizen_role on dbs.dashboard_notifications(reference, citizen_role);


create index idx_task_item_template_scenario_name on dbs.task_item_template(scenario_name);
create index idx_task_item_template_template_name_role on dbs.task_item_template(template_name, role);
create index idx_task_item_template_role on dbs.task_item_template(role);

create index idx_task_list_reference on dbs.task_list(reference);
