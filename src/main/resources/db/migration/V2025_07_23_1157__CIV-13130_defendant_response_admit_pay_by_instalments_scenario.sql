/**
 * Update scenario
 */
UPDATE dbs.scenario
SET notifications_to_create = '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant":["respondent1PartyName", "defendantAdmittedAmount", "claimantAmountIncludesTextEn", "claimantAmountIncludesTextCy", "installmentAmount", "paymentFrequency","paymentFrequencyWelsh","firstRepaymentDateEn", "firstRepaymentDateCy"]}'
WHERE name = 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant';

/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En ='<p class="govuk-body">${respondent1PartyName} has offered to pay you ${defendantAdmittedAmount} ${claimantAmountIncludesTextEn} in instalments of ${installmentAmount} ${paymentFrequency}. They are offering to do this starting from ${firstRepaymentDateEn}.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" rel="noopener noreferrer" class="govuk-link">View and respond</a></p>',
  description_Cy='<p class="govuk-body">Mae ${respondent1PartyName} wedi cynnig talu ${defendantAdmittedAmount} ${claimantAmountIncludesTextCy} i chi mewn rhandaliadau o ${installmentAmount} ${paymentFrequencyWelsh}. Maent yn cynnig gwneud hyn o ${firstRepaymentDateCy} ymlaen.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" rel="noopener noreferrer" class="govuk-link">Gweld ac ymateb</a></p>'
WHERE template_name = 'Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant' AND notification_role ='CLAIMANT';
