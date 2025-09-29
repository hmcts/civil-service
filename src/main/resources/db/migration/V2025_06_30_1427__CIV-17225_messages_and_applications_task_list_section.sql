INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Applications.to.the.court', '{}', '{}');

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Messages.to.the.court', '{}', '{}');

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ViewAvailableMessages', '{}', '{}');


INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View all applications to the court</a>',
        'Applications to the court', '<a>Gweld pob cais i’r llys</a>',
        'Ceisiadau i’r llys', 'View.Applications', 'Scenario.AAA6.Applications.to.the.court',
        '{1, 1}', 'CLAIMANT', 17),
       ('<a>View all applications to the court</a>',
        'Applications to the court', '<a>Gweld pob cais i’r llys</a>',
        'Ceisiadau i’r llys', 'View.Applications', 'Scenario.AAA6.Applications.to.the.court',
        '{1, 1}', 'DEFENDANT', 17),
       ('<a>View all messages to the court</a>', 'Messages to the court',
        '<a>Gweld yr holl negeseuon i’r llys</a>',
        'Negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.Messages.to.the.court',
        '{1, 1}', 'CLAIMANT', 18),
       ('<a>View all messages to the court</a>', 'Messages to the court',
        '<a>Gweld yr holl negeseuon i’r llys</a>',
        'Negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.Messages.to.the.court',
        '{1, 1}', 'DEFENDANT', 18),
       ('<a href="{QM_VIEW_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">View all messages to the court</a>',
        'Messages to the court',
        '<a href="{QM_VIEW_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">Gweld yr holl negeseuon i’r llys</a>',
        'Negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.ViewAvailableMessages',
        '{3, 3}', 'CLAIMANT', 18),
       ('<a href="{QM_VIEW_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">View all messages to the court</a>',
        'Messages to the court',
        '<a href="{QM_VIEW_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">Gweld yr holl negeseuon i’r llys</a>',
        'Negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.ViewAvailableMessages',
        '{3, 3}', 'DEFENDANT', 18);
