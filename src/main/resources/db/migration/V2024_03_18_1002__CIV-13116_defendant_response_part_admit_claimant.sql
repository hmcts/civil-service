/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant',
        '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.PhonePayment","Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}',
        '{"Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant" : ["respondent1PartyName", "defendantAdmittedAmount","respondent1AdmittedAmountPaymentDeadlineEn","respondent1AdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant',
        'Response to the claim', 'Ymateb i’r hawliad',
        '<p class="govuk-body">${respondent1PartyName} has offered to pay ${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p><p class="govuk-body">The payment must be received in your account by then, if not you can request a county court judgment.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">View and respond</a></p>',
        '<p class="govuk-body">Mae ${respondent1PartyName} wedi cynnig talu ${defendantAdmittedAmount} erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}.</p><p class="govuk-body">Rhaid i’r taliad fod yn eich cyfrif erbyn y dyddiad hwnnw. Os nad yw, yna gallwch wneud cais am ddyfarniad llys sirol.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">Gweld ac ymateb</a></p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role,task_order)
values
  ('<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>',
   'The response',
   '<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
   'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant', '{3, 3}', 'CLAIMANT', 3)
