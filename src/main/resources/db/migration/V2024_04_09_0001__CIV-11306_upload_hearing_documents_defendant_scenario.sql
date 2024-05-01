/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingDocuments.Upload.Defendant',
        '{}',
        '{"Notice.AAA6.CP.HearingDocuments.Upload.Defendant" : ["sdoDocumentUploadRequestedDateEn", "sdoDocumentUploadRequestedDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.HearingDocuments.Upload.Defendant', 'Upload documents', 'Upload documents',
        '<p class="govuk-body">You can <a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">upload and submit documents</a> to support your defence. Follow the instructions set out in the directions order. You must submit all documents by ${sdoDocumentUploadRequestedDateEn}. Any documents submitted after the deadline may not be considered by the judge.</p>',
        '<p class="govuk-body">Gallwch <a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">upload and submit documents</a> i gefnogi eich amddiffyniad. Dilynwch y cyfarwyddiadau a nodir yn y gorchymyn cyfarwyddiadau. Rhaid i chi gyflwyno’r holl ddogfennau erbyn ${sdoDocumentUploadRequestedDateCy}. Mae''n bosib na fydd y barnwr yn ystyried unrhyw ddogfennau a gyflwynir ar ôl y dyddiad hwn.</p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">Upload hearing documents</a>', 'Hearing',
        '<a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">Upload hearing documents</a>', 'Hearing',
        'Hearing.Document.Upload', 'Scenario.AAA6.CP.HearingDocuments.Upload.Defendant',
        '{5, 5}', 'DEFENDANT', 2,
        'Deadline is 4pm on ${sdoDocumentUploadRequestedDateEn}',
        'Deadline is 4pm on ${sdoDocumentUploadRequestedDateCy}');

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a>View documents</a>', 'Hearing' ,'<a>View documents</a>', 'Hearing',
        'Hearing.Document.View', 'Scenario.AAA6.CP.HearingDocuments.Upload.Defendant',
        '{1, 1}', 'DEFENDANT', 3);
