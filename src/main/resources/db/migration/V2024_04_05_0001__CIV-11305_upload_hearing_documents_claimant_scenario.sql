/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingDocuments.Upload.Claimant',
        '{}',
        '{"Notice.AAA6.CP.HearingDocuments.Upload.Claimant" : ["sdoDocumentUploadRequestedDate"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.HearingDocuments.Upload.Claimant', 'An order has been made', 'An order has been made',
        '<p class="govuk-body">You can <a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">upload and submit documents.</a> to support your claim. Follow the instructions set out in the directions order. You must submit all documents by ${sdoDocumentUploadRequestedDate}. Any documents submitted after the deadline may not be considered by the judge.</p>',
        '<p class="govuk-body">You can <a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">upload and submit documents.</a> to support your claim. Follow the instructions set out in the directions order. You must submit all documents by ${sdoDocumentUploadRequestedDate}. Any documents submitted after the deadline may not be considered by the judge.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a>Upload hearing documents</a>', 'Hearings' ,'<a>Upload hearing documents</a>', 'Hearings',
        'Notice.AAA6.CP.HearingDocuments.Upload.Claimant', 'Scenario.AAA6.CP.HearingDocuments.Upload.Claimant',
        '{2, 2}', 'CLAIMANT', 6);
