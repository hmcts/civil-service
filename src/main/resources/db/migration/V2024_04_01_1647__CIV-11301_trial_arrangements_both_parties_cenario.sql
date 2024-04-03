/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Trial.Arrangements.Required.BothParties ',
        '{"Notice.AAA6.CP.Hearing.Scheduled.Both Parties"}',
        '{"Notice.AAA6.CP.Trial.Arrangements.Required.BothParties" : ["respondent1ResponseDeadlineEn", "respondent1ResponseDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.Trial.Arrangements.Required.BothParties', 'Confirm your trial arrangements', 'Confirm your trial arrangements',
        '<p class="govuk-body">You must <a href="{TRIAL_ARRANGEMENTS}" class="govuk-link">confirm your trial arrangements</a> by ${respondent1ResponseDeadlineEn}. ' ||
        'This means that you’ll need to confirm if the case is ready for trial or not. ' ||
        'You’ll also need to confirm whether circumstances have changed since you completed the directions questionnaire. ' ||
        'Refer to the <a href="{QUESTIONNAIRE_SUBMITTED}" class="govuk-link">questionnaire you submitted</a> if you’re not sure what you previously said.</p>',
        '<p class="govuk-body">You must <a href="{TRIAL_ARRANGEMENTS}" class="govuk-link">confirm your trial arrangements</a> by ${respondent1ResponseDeadlineCy}. ' ||
        'This means that you’ll need to confirm if the case is ready for trial or not. ' ||
        'You’ll also need to confirm whether circumstances have changed since you completed the directions questionnaire. ' ||
        'Refer to the <a href="{QUESTIONNAIRE_SUBMITTED}" class="govuk-link">questionnaire you submitted</a> if you’re not sure what you previously said.</p>';

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response', 'Response.View', 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant', '{3, 3}', 'DEFENDANT', 3);
