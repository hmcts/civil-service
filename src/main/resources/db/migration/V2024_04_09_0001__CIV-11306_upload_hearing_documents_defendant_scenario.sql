/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingDocuments.Upload.Defendant',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant",
          "Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant"}',
        '{"Notice.AAA6.CP.HearingDocuments.Upload.Defendant" : ["sdoDocumentUploadRequestedDateEn", "sdoDocumentUploadRequestedDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.HearingDocuments.Upload.Defendant', 'Upload documents', 'Llwytho dogfennau',
        '<p class="govuk-body">You can <a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">upload and submit documents</a> to support your defence. Follow the instructions set out in the <a href="{VIEW_ORDERS_AND_NOTICES}" class="govuk-link">directions order</a>. Any documents submitted after the deadlines in the directions order may not be considered by the judge.</p>',
        '<p class="govuk-body">Gallwch <a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">lwytho a chyflwyno dogfennau</a> i gefnogi eich amddiffyniad. Dilynwch y cyfarwyddiadau a nodir yn y <a href="{VIEW_ORDERS_AND_NOTICES}" class="govuk-link">gorchymyn cyfarwyddiadau</a>. Ni chaiff y barnwr ystyried unrhyw ddogfennau a gyflwynir ar ôl y dyddiadau cau yn y gorchymyn cyfarwyddiadau.</p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">Upload hearing documents</a>', 'Hearing',
        '<a href="{UPLOAD_HEARING_DOCUMENTS}" class="govuk-link">Llwytho dogfennau''r gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.Document.Upload', 'Scenario.AAA6.CP.HearingDocuments.Upload.Defendant',
        '{5, 6}', 'DEFENDANT', 9);
