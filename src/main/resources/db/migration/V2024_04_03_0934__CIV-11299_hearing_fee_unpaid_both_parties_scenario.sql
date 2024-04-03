/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant',
        '{"Notice.AAA6.CP.Hearing.Scheduled.BothParties"}',
        '{"Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant" : ["hearingDueDateEn", "hearingDueDateCy"]}'),
       ('Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant',
        '{"Notice.AAA6.CP.Hearing.Scheduled.BothParties"}',
        '{"Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant" : ["hearingDueDateEn", "hearingDueDateCy"]}'),
        ('Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Claimant',
        '{"Notice.AAA6.CP.Hearing.Scheduled.BothParties"}',
        '{"Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant" : ["hearingDueDateEn", "hearingDueDateCy"]}'),
       ('Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Defendant',
        '{"Notice.AAA6.CP.Hearing.Scheduled.BothParties"}',
        '{"Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant" : ["hearingDueDateEn", "hearingDueDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant', 'The claim has been struck out', 'The claim has been struck out',
        '<p class="govuk-body">This is because the hearing fee was not paid by ${hearingDueDateEn} as stated in the <a href="{VIEW_HEARING_NOTICE}" class="govuk-link">hearing notice.</a></p>',
        '<p class="govuk-body">This is because the hearing fee was not paid by ${hearingDueDateCy} as stated in the <a href="{VIEW_HEARING_NOTICE}" class="govuk-link">hearing notice.</a></p>',
        'CLAIMANT'),
       ('Notice.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant', 'The claim has been struck out', 'The claim has been struck out',
        '<p class="govuk-body">This is because the hearing fee was not paid by ${hearingDueDateEn} as stated in the <a href="{VIEW_HEARING_NOTICE}" class="govuk-link">hearing notice.</a></p>',
        '<p class="govuk-body">This is because the hearing fee was not paid by ${hearingDueDateCy} as stated in the <a href="{VIEW_HEARING_NOTICE}" class="govuk-link">hearing notice.</a></p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a>Upload hearing documents</a>', 'Hearings' ,'<a>Upload hearing documents</a>',
        'Hearings', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant', '{1, 1}', 'CLAIMANT', 6),
       ('<a>Add the trial arrangements</a>', 'Hearings' ,'<a>Add the trial arrangements</a>',
        'Hearings', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant', '{1, 1}', 'CLAIMANT', 7),
       ('<a>Pay the hearing fee</a>', 'Hearings' ,'<a>Pay the hearing fee</a>',
        'Hearings', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant', '{1, 1}', 'CLAIMANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearings' ,'<a>Upload hearing documents</a>',
        'Hearings', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant', '{1, 1}', 'DEFENDANT', 6),
       ('<a>Add the trial arrangements</a>', 'Hearings' ,'<a>Add the trial arrangements</a>',
        'Hearings', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Defendant', '{1, 1}', 'DEFENDANT', 7),
       ('<a>Upload hearing documents</a>', 'Hearings' ,'<a>Upload hearing documents</a>',
        'Hearings', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Claimant', '{1, 1}', 'CLAIMANT', 6),
       ('<a>Pay the hearing fee</a>', 'Hearings' ,'<a>Pay the hearing fee</a>',
        'Hearings', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Claimant', '{1, 1}', 'CLAIMANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearings' ,'<a>Upload hearing documents</a>',
        'Hearings', 'Hearing.Document.Upload', 'Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Defendant', '{1, 1}', 'DEFENDANT', 6);
