/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En ='<p class="govuk-body">${respondent1PartyName} has offered to pay ${defendantAdmittedAmount} plus the claim fee by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p><p class="govuk-body">The payment must be received in your account by then, if not you can request a county court judgment.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">View and respond</a></p>',
    description_Cy='<p class="govuk-body">Mae ${respondent1PartyName} wedi cynnig talu ${defendantAdmittedAmount} ynghyd â ffi’r hawliad erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}.</p><p class="govuk-body">Rhaid i’r taliad fod yn eich cyfrif erbyn y dyddiad hwnnw. Os nad yw, yna gallwch wneud cais am ddyfarniad llys sirol.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">Gweld ac ymateb</a></p>'
WHERE template_name = 'Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant' and notification_role='CLAIMANT';
