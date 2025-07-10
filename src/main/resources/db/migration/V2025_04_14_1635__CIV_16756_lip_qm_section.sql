INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Applications.and.messages.to.the.court', '{}', '{}');


INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View all applications to the court</a>',
        'Applications and messages to the court', '<a>Gweld pob cais i’r llys</a>',
        'Ceisiadau a negeseuon i’r llys', 'View.Applications', 'Scenario.AAA6.Applications.and.messages.to.the.court',
        '{1, 1}', 'CLAIMANT', 17),
       ('<a>View your messages to the court</a>', 'Applications and messages to the court',
        '<a>Gweld eich negeseuon i''r llys</a>',
        'Ceisiadau a negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.Applications.and.messages.to.the.court',
        '{1, 1}', 'CLAIMANT', 18),
       ('<a>View all applications to the court</a>',
        'Applications and messages to the court', '<a>Gweld pob cais i’r llys</a>',
        'Ceisiadau a negeseuon i’r llys', 'View.Applications', 'Scenario.AAA6.Applications.and.messages.to.the.court',
        '{1, 1}', 'DEFENDANT', 17),
       ('<a>View your messages to the court</a>', 'Applications and messages to the court',
        '<a>Gweld eich negeseuon i''r llys</a>',
        'Ceisiadau a negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.Applications.and.messages.to.the.court',
        '{1, 1}', 'DEFENDANT', 18);
