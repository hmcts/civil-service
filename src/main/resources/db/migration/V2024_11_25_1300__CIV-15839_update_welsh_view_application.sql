/**
 * Updating Welsh
 */
UPDATE dbs.task_item_template SET task_name_cy = replace(task_name_cy, '<a>Gweld y cais i gyd</a>', '<a>Gweld ceisiadau</a>')
                                             WHERE template_name in ('Application.View');

UPDATE dbs.task_item_template SET task_name_cy = replace(task_name_cy, '<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>', '<a href={GENERAL_APPLICATIONS_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld ceisiadau</a>')
                                            WHERE template_name in ('Application.View');

UPDATE dbs.task_item_template SET task_name_cy = replace(task_name_cy, '<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld y cais i gyd</a>', '<a href={GENERAL_APPLICATIONS_RESPONSE_APPLICATION_SUMMARY_URL} rel="noopener noreferrer" class="govuk-link">Gweld ceisiadau</a>')
                                            WHERE template_name in ('Application.View');
