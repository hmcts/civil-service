/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApplication.Created.Claimant', '{}', '{}'),
       ('Scenario.AAA6.GeneralApplication.Created.Defendant', '{}', '{}'),
       ('Scenario.AAA6.GeneralApplication.Complete.Claimant', '{}', '{}'),
       ('Scenario.AAA6.GeneralApplication.Complete.Defendant', '{}', '{}');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.Created.Claimant', '{6, 6}', 'CLAIMANT', 17),
       ('<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.Created.Defendant', '{6, 6}', 'DEFENDANT', 17),
       ('<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.Complete.Claimant', '{3, 3}', 'CLAIMANT', 17),
       ('<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.Complete.Defendant', '{3, 3}', 'DEFENDANT', 17);
