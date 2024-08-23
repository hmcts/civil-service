/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApplication.Complete.Claimant', '{"Notice.AAA6.GeneralApplication.Fee.Required"}', '{"Notice.AAA6.GeneralApplication.submitted"}'),
       ('Scenario.AAA6.GeneralApplication.Complete.Defendant', '{}', '{}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApplication.submitted', 'Your application has been submitted', 'Your application has been submitted',
        '<p class="govuk-body">Your Application has been submitted.</p>',
        '<p class="govuk-body">Your Application has been submitted.</p>',
        'CLAIMANT');

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
