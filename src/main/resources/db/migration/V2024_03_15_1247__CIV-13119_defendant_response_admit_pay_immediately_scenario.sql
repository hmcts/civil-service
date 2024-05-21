/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant', '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}', '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant" : ["defendantAdmittedAmount", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy", "applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant', 'Response to the claim', 'Response to the claim',
        '<p class="govuk-body">You have offered to pay ${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineEn}. The payment must be received in ${applicant1PartyName}''s account by then, if not they can request a county court judgment.</p><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a>.',
        '<p class="govuk-body">You have offered to pay ${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineCy}. The payment must be received in ${applicant1PartyName}''s account by then, if not they can request a county court judgment.</p><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a>.',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant', '{3, 3}', 'DEFENDANT', 3);
