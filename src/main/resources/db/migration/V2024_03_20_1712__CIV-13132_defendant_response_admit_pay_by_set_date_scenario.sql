/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant', '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoretimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}', '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant" : ["defendantAdmittedAmount", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant', 'Response to the claim', 'Response to the claim',
        '<p class="govuk-body">You have offered to pay ${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineEn}. The court will contact you when they respond.</p><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a>.',
        '<p class="govuk-body">You have offered to pay ${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineCy}. The court will contact you when they respond.</p><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a>.',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response', 'Response.View', 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant', '{3, 3}', 'DEFENDANT', 3);
