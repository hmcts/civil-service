/**
 * Update scenario
 */
UPDATE dbs.scenario
SET notifications_to_create = '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant" :  ["defendantAdmittedAmount", "defendantAmountIncludesTextEn", "defendantAmountIncludesTextCy", "installmentAmount", "paymentFrequency","paymentFrequencyWelsh","firstRepaymentDateEn","firstRepaymentDateCy"]}'
WHERE name = 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Defendant';

/**
 * UPDATE notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">You have offered to pay ${defendantAdmittedAmount} ${defendantAmountIncludesTextEn} in instalments of ${installmentAmount} ${paymentFrequency}. You have offered to do this starting from ${firstRepaymentDateEn}. We will contact you when the claimant responds to your offer.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a></p>',
    description_Cy = '<p class="govuk-body">Rydych wedi cynnig talu ${defendantAdmittedAmount} ${defendantAmountIncludesTextCy} mewn rhandaliadau o ${installmentAmount} ${paymentFrequencyWelsh}. Rydych wedi cynnig gwneud hyn o ${firstRepaymentDateCy} ymlaen. Byddwn yn cysylltu â chi pan fydd yr hawlydd yn ymateb i’ch cynnig.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">Gweld eich ymateb</a></p>'
WHERE template_name = 'Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant' and notification_role = 'DEFENDANT';
