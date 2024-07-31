/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingDocuments.Uploaded.Claimant',
        '{Notice.AAA6.CP.HearingDocuments.Upload.Claimant}',
        '{}'),
       ('Scenario.AAA6.CP.HearingDocuments.NotUploaded.Claimant',
        '{}',
        '{}'),
       ('Scenario.AAA6.CP.HearingDocuments.Uploaded.Defendant',
        '{Notice.AAA6.CP.HearingDocuments.Upload.Defendant}',
        '{}'),
       ('Scenario.AAA6.CP.HearingDocuments.NotUploaded.Defendant',
        '{}',
        '{}');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a href="{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}" class="govuk-link">View documents</a>', 'Hearing' ,'<a href="{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}" class="govuk-link">Gweld y dogfennau</a>',
        'Gwrandawiad', 'Hearing.Document.View', 'Scenario.AAA6.CP.HearingDocuments.Uploaded.Claimant','{3, 3}', 'CLAIMANT', 11, null, null),
        ('<a href="{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}" class="govuk-link">View documents</a>', 'Hearing' ,'<a href="{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}" class="govuk-link">Gweld y dogfennau</a>',
        'Gwrandawiad', 'Hearing.Document.View', 'Scenario.AAA6.CP.HearingDocuments.NotUploaded.Claimant','{3, 3}', 'CLAIMANT', 11, null, null),
       ('<a href="{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}" class="govuk-link">View documents</a>', 'Hearing' ,'<a href="{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}" class="govuk-link">Gweld y dogfennau</a>',
        'Gwrandawiad', 'Hearing.Document.View', 'Scenario.AAA6.CP.HearingDocuments.NotUploaded.Defendant','{3, 3}', 'DEFENDANT', 11, null, null),
        ('<a href="{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}" class="govuk-link">View documents</a>', 'Hearing' ,'<a href="{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}" class="govuk-link">Gweld y dogfennau</a>',
        'Gwrandawiad', 'Hearing.Document.View', 'Scenario.AAA6.CP.HearingDocuments.Uploaded.Defendant','{3, 3}', 'DEFENDANT', 11, null, null);
