/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingFee.Required.Claimant', '{}', '{"Notice.AAA6.CP.HearingFee.Required.Claimant" : ["hearingFee", "hearingDueDateEn", "hearingDueDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.HearingFee.Required.Claimant', 'You must pay the hearing fee', 'You must pay the hearing fee',
        '<p class="govuk-body">You must either <a href="{PAY_HEARING_FEE_URL_REDIRECT}" class="govuk-link">pay the hearing fee</a> of ${hearingFee}' ||
        ' or <a href="{APPLY_HELP_WITH_FEES_START_REDIRECT}" class="govuk-link"> apply for help with fees</a>.' ||
        ' You must do this by ${hearingDueDateEn}. If you do not take one of these actions, your claim will be struck out.',
        '<p class="govuk-body">You must either <a href="{PAY_HEARING_FEE_URL_REDIRECT}" class="govuk-link">pay the hearing fee</a> of ${hearingFee}' ||
        ' or <a href="{APPLY_HELP_WITH_FEES_START_REDIRECT}" class="govuk-link"> apply for help with fees</a>.' ||
        ' You must do this by ${hearingDueDateCy}. If you do not take one of these actions, your claim will be struck out.',
        'CLAIMANT', 'Session');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={PAY_HEARING_FEE} class="govuk-link">Pay the hearing fee</a>', 'Hearings' ,'<a href={PAY_HEARING_FEE} class="govuk-link">Pay the hearing fee</a>',
        'Hearings', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.HearingFee.Required.Claimant', '{3, 3}', 'CLAIMANT', 8);
