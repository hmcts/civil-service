/**
 * Update scenario
 */
UPDATE dbs.scenario
SET notifications_to_create = '{"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant": ["respondent1PartyName", "defendantAdmittedAmount", "claimantAmountIncludesTextEn", "claimantAmountIncludesTextCy", "installmentAmount", "paymentFrequency","paymentFrequencyWelsh","firstRepaymentDateEn","firstRepaymentDateCy"]}'
WHERE name = 'Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant';

/**
 * UPDATE notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">${respondent1PartyName} has offered to pay you ${defendantAdmittedAmount} ${claimantAmountIncludesTextEn} in instalments of ${installmentAmount} ${paymentFrequency}. They are offering to do this starting from ${firstRepaymentDateEn}. The defendant needs to send you their financial details.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" rel="noopener noreferrer" class="govuk-link">View and respond</a></p>',
    description_Cy = '<p class="govuk-body">Mae ${respondent1PartyName} wedi cynnig talu ${defendantAdmittedAmount} ${claimantAmountIncludesTextCy} i chi mewn rhandaliadau o ${installmentAmount} ${paymentFrequencyWelsh}. Maent yn cynnig gwneud hyn o ${firstRepaymentDateCy} ymlaen. Mae angen i’r diffynnydd anfon eu manylion ariannol atoch.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" rel="noopener noreferrer" class="govuk-link">Gweld ac ymateb</a></p>'
WHERE template_name = 'Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant' AND notification_role = 'CLAIMANT';
