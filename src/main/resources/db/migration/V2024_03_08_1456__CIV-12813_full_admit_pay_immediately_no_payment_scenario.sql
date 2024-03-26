/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.FullAdmit.Claimant', '{"Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant"}', '{"Notice.AAA6.ClaimantIntent.FullAdmit.Claimant": ["respondent1PartyName", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.FullAdmit.Claimant', 'Immediate payment', 'Immediate payment',
        '<p class="govuk-body">You have accepted ${respondent1PartyName}''s plan to pay £{fullAdmitPayImmediatelyPaymentAmount} immediately. Funds must clear your account by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p><p class="govuk-body">If you don''t receive the money by then, you can <a href="{COUNTY_COURT_JUDGEMENT_URL}"  rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a>.</p>',
        '<p class="govuk-body">You have accepted ${respondent1PartyName}''s plan to pay £{fullAdmitPayImmediatelyPaymentAmount} immediately. Funds must clear your account by ${respondent1AdmittedAmountPaymentDeadlineCy}.</p><p class="govuk-body">If you don''t receive the money by then, you can <a href="{COUNTY_COURT_JUDGEMENT_URL}"  rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a>.</p>',
        'CLAIMANT');
