/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApplication.InitiateApplication.Inactive.Claimant', '{}', '{}'),
       ('Scenario.AAA6.GeneralApplication.InitiateApplication.Inactive.Defendant', '{}', '{}');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('Contact the court to request a change to my case',
        'Applications', 'Cysylltu â’r llys i wneud cais am newid i fy achos',
        'Ceisiadau', 'Application.Create', 'Scenario.AAA6.GeneralApplication.InitiateApplication.Inactive.Claimant',
        '{2, 2}', 'CLAIMANT', 16),
       ('Contact the court to request a change to my case',
        'Applications', 'Cysylltu â’r llys i wneud cais am newid i fy achos',
        'Ceisiadau', 'Application.Create', 'Scenario.AAA6.GeneralApplication.InitiateApplication.Inactive.Defendant',
        '{2, 2}', 'DEFENDANT', 16);
