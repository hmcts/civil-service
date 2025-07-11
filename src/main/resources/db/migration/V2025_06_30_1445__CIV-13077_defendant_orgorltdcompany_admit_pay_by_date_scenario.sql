/**
 * UPDATE scenario
 */
UPDATE dbs.scenario
SET notifications_to_create = '{"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant" : ["respondent1PartyName", "defendantAdmittedAmount",
          "claimantAmountIncludesTextEn", "claimantAmountIncludesTextCy", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}'
WHERE name = 'Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant';

/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">${respondent1PartyName} has offered to pay you ${defendantAdmittedAmount} ${claimantAmountIncludesTextEn} by ${respondent1AdmittedAmountPaymentDeadlineEn}. The defendant needs to send you their financial details.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">View and respond</a></p>',
    description_Cy = '<p class="govuk-body">Mae ${respondent1PartyName} wedi cynnig talu ${defendantAdmittedAmount} ${claimantAmountIncludesTextCy} i chi erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}. Mae angen i’r diffynnydd anfon eu manylion ariannol atoch.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">Gweld ac ymateb</a></p>'
WHERE template_name = 'Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant' AND notification_role = 'CLAIMANT';
