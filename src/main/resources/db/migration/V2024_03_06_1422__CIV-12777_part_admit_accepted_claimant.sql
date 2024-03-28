INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.PartAdmit.Claimant',
        '{"Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant"}', '{"Notice.AAA6.ClaimantIntent.PartAdmit.Claimant":["respondent1PartyName", "defendantAdmittedAmount", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.PartAdmit.Claimant', 'Immediate payment', 'Immediate payment',
        '<p class="govuk-body">${respondent1PartyName} said they will pay you ${defendantAdmittedAmount} immediately. Funds must clear your account by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p> <p class="govuk-body">If you don´t receive the money by then, you can <a href={COUNTY_COURT_JUDGEMENT_URL} class="govuk-link">request a County Court Judgment</a>.</p>',
        '<p class="govuk-body">${respondent1PartyName} said they will pay you ${defendantAdmittedAmount} immediately. Funds must clear your account by ${respondent1AdmittedAmountPaymentDeadlineCy}.</p> <p class="govuk-body">If you don´t receive the money by then, you can <a href={COUNTY_COURT_JUDGEMENT_URL} class="govuk-link">request a County Court Judgment</a>.</p>',
        'CLAIMANT');
