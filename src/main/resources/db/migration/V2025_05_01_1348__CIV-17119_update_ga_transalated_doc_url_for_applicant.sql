/**
 * Removing full stop and new line from link
 */
UPDATE dbs.dashboard_notifications_templates SET description_En = replace(description_En, '<p class="govuk-body"><a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">View translated application documents.</a></p>')
                                             WHERE template_name in ('Notice.AAA6.GeneralApps.TranslatedDocumentUploaded.Applicant');
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, '<p class="govuk-body"><a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Gweld dogfennau’r cais a gyfieithwyd.</a></p>')
                                             WHERE template_name in ('Notice.AAA6.GeneralApps.TranslatedDocumentUploaded.Applicant');
