/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant',
        '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant"}',
        '{"Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant" : ["defendantAdmittedAmount","respondent1AdmittedAmountPaymentDeadlineEn","respondent1AdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant',
        'Response to the claim', 'Response to the claim',
        '<p class="govuk-body">The defendant has offered to pay ${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineEn}.<br>The payment must clear the account by then, if not you can request a county court judgment.<br><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">View and respond</a></p>',
        '<p class="govuk-body">The defendant has offered to pay ${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineCy}.<br>The payment must clear the account by then, if not you can request a county court judgment.<br><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">View and respond</a></p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role,task_order)
values
  ('<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>',
   'The response',
   '<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>',
   'The response', 'Response.View', 'Scenario.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant', '{3, 3}', 'CLAIMANT', 3)
