/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingFee.HWF.Rejected', '{"Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant", "Notice.AAA6.CP.HearingFee.HWF.InvalidRef.Claimant", "Notice.AAA6.CP.HearingFee.HWF.InfoRequired.Claimant", "Notice.AAA6.CP.HearingFee.HWF.ReviewUpdate.Claimant"}',
        '{"Notice.AAA6.CP.HearingFee.HWF.Rejected" : ["hearingFee", "hearingDueDateEn", "hearingDueDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.CP.HearingFee.HWF.Rejected', 'Your help with fees application has been rejected',
        'Your help with fees application has been rejected',
        '<p class="govuk-body">We''ve rejected your application for help with the hearing fee. See the email for further details.</p><p class="govuk-body">You must <a href={PAY_HEARING_FEE} class="govuk-link">pay the full fee</a> of ${hearingFee} by ${hearingDueDateEn}. You can pay by phone by calling {civilMoneyClaimsTelephone}. If you do not pay your claim will be struck out.</p>',
        '<p class="govuk-body">We''ve rejected your application for help with the hearing fee. See the email for further details.</p><p class="govuk-body">You must <a href={PAY_HEARING_FEE} class="govuk-link">pay the full fee</a> of ${hearingFee} by ${hearingDueDateCy}. You can pay by phone by calling {civilMoneyClaimsTelephone}. If you do not pay your claim will be struck out.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a href={PAY_HEARING_FEE} class="govuk-link">Pay the hearing fee</a>', 'Hearings' ,'<a href={PAY_HEARING_FEE} class="govuk-link">Pay the hearing fee</a>',
        'Hearings', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.HearingFee.HWF.Rejected', '{5, 5}', 'CLAIMANT', 8, 'Deadline is 12am on ${hearingDueDateEn}', 'Deadline is 12am on ${hearingDueDateCy}');
