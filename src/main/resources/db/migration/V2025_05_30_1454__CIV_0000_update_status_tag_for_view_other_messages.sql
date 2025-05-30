/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a href="{QM_VIEW_OTHER_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">View messages from other parties</a>',
        'Applications and messages to the court',
        '<a href="{QM_VIEW_OTHER_MESSAGES_URL}" rel="noopener noreferrer" class="govuk-link">Gweld negeseuon gan bartïon eraill</a>',
        'Ceisiadau a negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.ViewMessages.Available.Defendant',
        '{3, 3}', 'CLAIMANT', 19),
       ('<a href="{QM_VIEW_OTHER_MESSAGES}" rel="noopener noreferrer" class="govuk-link">View messages from other parties</a>',
        'Applications and messages to the court',
        '<a href="{QM_VIEW_OTHER_MESSAGES}" rel="noopener noreferrer" class="govuk-link">Gweld negeseuon gan bartïon eraill</a>',
        'Ceisiadau a negeseuon i’r llys', 'View.Messages', 'Scenario.AAA6.ViewMessages.Available.Claimant',
        '{3, 3}', 'DEFENDANT', 19);
