/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingDocuments.Upload.Defendant',
        '{}',
        '{"Notice.AAA6.CP.HearingDocuments.Upload.Defendant" : ["sdoDocumentUploadRequestedDate"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.HearingDocuments.Upload.Defendant', 'An order has been made', 'An order has been made',
        '<p class="govuk-body">You can <a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">upload and submit documents.</a> to support your defence. Follow the instructions set out in the directions order. You must submit all documents by ${sdoDocumentUploadRequestedDate}. Any documents submitted after the deadline may not be considered by the judge.</p>',
        '<p class="govuk-body">You can <a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">upload and submit documents.</a> to support your defence. Follow the instructions set out in the directions order. You must submit all documents by ${sdoDocumentUploadRequestedDate}. Any documents submitted after the deadline may not be considered by the judge.</p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href="{UPLOAD_HEARING_DOCUMENTS}">Upload hearing documents</a>', 'Hearings' ,'<a>Upload hearing documents</a>', 'Hearings',
        'Hearing.Document.Upload', 'Scenario.AAA6.CP.HearingDocuments.Upload.Defendant',
        '{5, 5}', 'DEFENDANT', 2),
       ('<a>View documents</a>', 'Hearings' ,'<a>View documents</a>', 'Hearings',
  'Hearing.Document.View', 'Scenario.AAA6.CP.HearingDocuments.Upload.Defendant',
  '{1, 1}', 'DEFENDANT', 3);
