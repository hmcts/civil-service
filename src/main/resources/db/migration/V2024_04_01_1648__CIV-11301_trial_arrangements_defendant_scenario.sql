/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Trial.Arrangements.Required.Defendant',
        '{"Notice.AAA6.CP.Hearing.Scheduled.Defendant"}',
        '{"Notice.AAA6.CP.Trial.Arrangements.Required.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.Trial.Arrangements.Required.Defendant', 'Confirm your trial arrangements', 'Confirm your trial arrangements',
        '<p class="govuk-body">You must <a href="{ADD_TRIAL_ARRANGEMENTS}" class="govuk-link">confirm your trial arrangements</a> by {HEARING_DUE_DATE}. ' ||
        'This means that you''ll need to confirm if the case is ready for trial or not. ' ||
        'You''ll also need to confirm whether circumstances have changed since you completed the directions questionnaire. ' ||
        'Refer to the <a href="{VIEW_DEFENDANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link" target="_blank">questionnaire you submitted</a> if you''re not sure what you previously said.</p>',
        '<p class="govuk-body">You must <a href="{ADD_TRIAL_ARRANGEMENTS}" class="govuk-link">confirm your trial arrangements</a> by {HEARING_DUE_DATE}. ' ||
        'This means that you''ll need to confirm if the case is ready for trial or not. ' ||
        'You''ll also need to confirm whether circumstances have changed since you completed the directions questionnaire. ' ||
        'Refer to the <a href="{VIEW_DEFENDANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link" target="_blank">questionnaire you submitted</a> if you''re not sure what you previously said.</p>', 'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a href={ADD_TRIAL_ARRANGEMENTS} class="govuk-link">Add the trial arrangements</a>',
        'Hearing',
        '<a href={ADD_TRIAL_ARRANGEMENTS} class="govuk-link">Add the trial arrangements</a>',
        'Hearing', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Trial.Arrangements.Required.Defendant', '{5, 5}', 'DEFENDANT', 4, 'Deadline is 12am on ${trialArrangementDeadlineEn}', 'Deadline is 12am on ${trialArrangementDeadlineCy}');
