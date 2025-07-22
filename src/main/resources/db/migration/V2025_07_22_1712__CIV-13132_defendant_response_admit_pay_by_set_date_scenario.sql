/**
 * Update scenario
 */
UPDATE dbs.scenario
SET notifications_to_create = '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant" : ["defendantAdmittedAmount", "defendantAmountIncludesTextEn" ,"defendantAmountIncludesTextCy", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}'
WHERE name = 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant';

/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">You have offered to pay ${defendantAdmittedAmount} ${defendantAmountIncludesTextEn} by ${respondent1AdmittedAmountPaymentDeadlineEn}. We will contact you when the claimant responds to your offer.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a></p>',
    description_Cy = '<p class="govuk-body">Rydych wedi cynnig talu ${defendantAdmittedAmount} ${defendantAmountIncludesTextCy} erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}. Byddwn yn cysylltu â chi pan fydd yr hawlydd yn ymateb i’ch cynnig.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">Gweld eich ymateb</a></p>'
WHERE template_name = 'Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant' AND notification_role = 'DEFENDANT';
