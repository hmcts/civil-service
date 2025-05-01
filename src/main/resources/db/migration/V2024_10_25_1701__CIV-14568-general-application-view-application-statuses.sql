/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApplication.ViewApplicationAvailable.Claimant', '{}', '{}'),
       ('Scenario.AAA6.GeneralApplication.ViewApplicationAvailable.Defendant', '{}', '{}'),
       ('Scenario.AAA6.GeneralApplication.ViewApplicationActionNeeded.Claimant', '{}', '{}'),
       ('Scenario.AAA6.GeneralApplication.ViewApplicationActionNeeded.Defendant', '{}', '{}'),
       ('Scenario.AAA6.GeneralApplication.ViewApplicationInProgress.Claimant', '{}', '{}'),
       ('Scenario.AAA6.GeneralApplication.ViewApplicationInProgress.Defendant', '{}', '{}');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.ViewApplicationAvailable.Claimant', '{3, 3}', 'CLAIMANT', 17),
       ('<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.ViewApplicationAvailable.Defendant', '{3, 3}', 'DEFENDANT', 17),
       ('<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.ViewApplicationActionNeeded.Claimant', '{5, 5}', 'CLAIMANT', 17),
       ('<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.ViewApplicationActionNeeded.Defendant', '{5, 5}', 'DEFENDANT', 17),
       ('<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.ViewApplicationInProgress.Claimant', '{6, 6}', 'CLAIMANT', 17),
       ('<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">View applications</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.GeneralApplication.ViewApplicationInProgress.Defendant', '{6, 6}', 'DEFENDANT', 17);
