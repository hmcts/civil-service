/**
 * UPDATE scenario
 */
UPDATE dbs.scenario
SET notifications_to_create = '{"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant" : ["applicant1PartyName", "defendantAdmittedAmount", "defendantAmountIncludesTextEn", "defendantAmountIncludesTextCy", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}'
WHERE name = 'Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant';


/**
 * UPDATE notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En ='<p class="govuk-body">You have offered to pay ${applicant1PartyName} ${defendantAdmittedAmount} ${defendantAmountIncludesTextEn} by ${respondent1AdmittedAmountPaymentDeadlineEn}. You need to send the claimant your financial details. The court will contact you when they respond.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a></p>',
    description_Cy ='<p class="govuk-body">Rydych wedi cynnig talu ${defendantAdmittedAmount} ${defendantAmountIncludesTextCy} i ${applicant1PartyName} erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}. Mae angen i chi anfon eich manylion ariannol at yr hawlydd. Bydd y llys yn cysylltu â chi pan fyddant yn ymateb.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">Gweld eich ymateb</a></p>'
WHERE template_name = 'Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant';
