/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant',
        '{}',
        '{"Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant" : ["hearingDueDateEn", "hearingDueDateCy"]}'),
       ('Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant',
        '{}',
        '{"Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant" : ["hearingDueDateEn", "hearingDueDateCy"]}'),
        ('Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Claimant',
         '{}',
        '{"Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant" : ["hearingDueDateEn", "hearingDueDateCy"]}'),
       ('Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Defendant',
        '{}',
        '{"Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant" : ["hearingDueDateEn", "hearingDueDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant', 'The claim has been struck out', 'Mae''r hawliad wedi cael ei ddileu',
        '<p class="govuk-body">This is because the hearing fee was not paid by ${hearingDueDateEn} as stated in the <a href="{VIEW_HEARING_NOTICE}" rel="noopener noreferrer" target="_blank" class="govuk-link">hearing notice</a>.</p>',
        '<p class="govuk-body">Y rheswm am hyn yw na thalwyd ffi''r gwrandawiad erbyn ${hearingDueDateCy} fel y nodir yn yr <a href="{VIEW_HEARING_NOTICE}" rel="noopener noreferrer" target="_blank" class="govuk-link">hysbysiad o wrandawiad</a>.</p>',
        'CLAIMANT'),
       ('Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant', 'The claim has been struck out', 'Mae''r hawliad wedi cael ei ddileu',
        '<p class="govuk-body">This is because the hearing fee was not paid by ${hearingDueDateEn} as stated in the <a href="{VIEW_HEARING_NOTICE}" rel="noopener noreferrer" target="_blank" class="govuk-link">hearing notice</a>.</p>',
        '<p class="govuk-body">Y rheswm am hyn yw na thalwyd ffi''r gwrandawiad erbyn ${hearingDueDateCy} fel y nodir yn yr <a href="{VIEW_HEARING_NOTICE}" rel="noopener noreferrer" target="_blank" class="govuk-link">hysbysiad o wrandawiad</a>.</p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant', '{2, 2}', 'CLAIMANT', 9),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant', '{2, 2}', 'CLAIMANT', 12),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant', '{2, 2}', 'DEFENDANT', 9),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant', '{2, 2}', 'DEFENDANT', 11),
       ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Claimant', '{2, 2}', 'CLAIMANT', 9),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Defendant', '{2, 2}', 'DEFENDANT', 9);
