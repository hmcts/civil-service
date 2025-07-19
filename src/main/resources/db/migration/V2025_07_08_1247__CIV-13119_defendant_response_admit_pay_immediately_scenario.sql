/**
 * Update scenario
 */
UPDATE dbs.scenario
SET notifications_to_create = '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant" : ["defendantAdmittedAmount", "respondent1AdmittedAmountPaymentDeadlineEn", "amountIncludesTextEn", "amountIncludesTextCy", "respondent1AdmittedAmountPaymentDeadlineCy", "applicant1PartyName"]}'
WHERE name ='Scenario.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant';

/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">You have offered to pay ${defendantAdmittedAmount} by ${respondent1AdmittedAmountPaymentDeadlineEn}${amountIncludesTextEn}. The payment must be received in ${applicant1PartyName}''s account by then, if not they can request a county court judgment.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a></p>',
    description_Cy = '<p class="govuk-body">Rydych wedi cynnig talu ${defendantAdmittedAmount} erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}${amountIncludesTextCy}. Rhaid i’r taliad fod yng nghyfrif ${applicant1PartyName} erbyn y dyddiad hwnnw. Os nad yw, yna gallant wneud cais am ddyfarniad llys sirol.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">Gweld eich ymateb</a></p>'
WHERE template_name = 'Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant' and notification_role = 'DEFENDANT';

