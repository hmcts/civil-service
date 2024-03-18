/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.FullAdmit.Claimant', '{"Notice.AAA7.ClaimIssue.Response.Await, Notice.AAA7.DefResponse.MoretimeRequested.Claimant"}', '{"Notice.AAA7.DefResponse.FullAdmit.PayImmediately.Claimant": ["defendantAdmittedAmount", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.DefResponse.FullAdmit.PayImmediately.Claimant', 'Response to the claim', 'Response to the claim',
        '<p class="govuk-body">The defendant has offered to pay £${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p><p class="govuk-body">The payment must clear the account by then, if not you can request a county court judgment.</p><p class="govuk-body"><a href="{TELL_US_IT_IS_SETTLED}" rel="noopener noreferrer" class="govuk-link">Tell us it''s paid</a></p>',
        '<p class="govuk-body">The defendant has offered to pay £${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineCy}.</p><p class="govuk-body">The payment must clear the account by then, if not you can request a county court judgment.</p><p class="govuk-body"><a href="{TELL_US_IT_IS_SETTLED}" rel="noopener noreferrer" class="govuk-link">Tell us it''s paid</a></p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_CLAIM_URL} rel="noopener noreferrer" class="govuk-link">View the claim</a>', 'The claim','<a href={VIEW_CLAIM_URL}>View the claim</a>',
        'The claim', 'Claim.View', 'Scenario.AAA7.ClaimantIntent.FullAdmit.Claimant', '{3, 3}', 'CLAIMANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT} rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>', 'The claim','<a href={VIEW_INFO_ABOUT_CLAIMANT_URL}>View information about the claimant</a>',
        'The claim', 'Claim.Claimant.Info', 'Scenario.AAA7.ClaimantIntent.FullAdmit.Claimant', '{3, 3}', 'CLAIMANT', 2),
       ('<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>','The response', '<a href={VIEW_RESPONSE_TO_CLAIM}>View the response to the claim</a>',
        'The response', 'Response.View', 'Scenario.AAA7.ClaimantIntent.FullAdmit.Claimant', '{3, 3}', 'CLAIMANT', 3),
       ('<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response','<a href={VIEW_INFO_ABOUT_DEFENDANT_URL}>View information about the defendant</a>',
        'The response', 'Response.Defendant.Info', 'Scenario.AAA7.ClaimantIntent.FullAdmit.Claimant', '{3, 3}', 'CLAIMANT', 4),
       ('<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,'<a href={VIEW_ORDERS_AND_NOTICES_URL}>View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA7.ClaimantIntent.FullAdmit.Claimant', '{3, 3}', 'CLAIMANT', 10);
