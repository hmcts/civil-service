/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">${respondent1PartyName} has offered to pay ${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineEn}. This amount includes interest if it has been claimed which may continue to accrue to the date of Judgment, settlement agreement or earlier payment.</p><p class="govuk-body">The payment must be received in your account by then, if not you can request a county court judgment.</p><p class="govuk-body"><a href="{TELL_US_IT_IS_SETTLED}" rel="noopener noreferrer" class="govuk-link">Tell us you''ve settled the claim</a></p>',
  description_Cy = '<p class="govuk-body">Mae ${respondent1PartyName} wedi cynnig talu ${defendantAdmittedAmount} erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}. Mae’r swm hwn yn cynnwys llog os yw wedi’i hawlio a gall barhau i gronni hyd dyddiad y Dyfarniad, y cytundeb setlo neu daliad cynharach.</p><p class="govuk-body">Rhaid i’r taliad fod yn eich cyfrif erbyn y dyddiad hwnnw. Os nad yw, yna gallwch wneud cais am ddyfarniad llys sirol.</p><p class="govuk-body"><a href="{TELL_US_IT_IS_SETTLED}" rel="noopener noreferrer" class="govuk-link">Tell us you''ve settled the claim</a></p>'
WHERE template_name = 'Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant' AND notification_role = 'CLAIMANT';
