/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ViewMessages.Available.Claimant', '{}', '{}'),
       ('Scenario.AAA6.ViewMessages.Available.Defendant', '{}', '{}');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a href="{QM_VIEW_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">View your messages to the court</a>',
        'Applications and messages to the court',
        '<a href="{QM_VIEW_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">Gweld eich negeseuon i''r llys</a>',
        'Ceisiadau a negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.ViewMessages.Available.Claimant',
        '{3, 3}', 'CLAIMANT', 18),
       ('<a href="{QM_VIEW_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">View your messages to the court</a>',
        'Applications and messages to the court',
        '<a href="{QM_VIEW_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">Gweld eich negeseuon i''r llys</a>',
        'Ceisiadau a negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.ViewMessages.Available.Defendant',
        '{3, 3}', 'DEFENDANT', 18);
