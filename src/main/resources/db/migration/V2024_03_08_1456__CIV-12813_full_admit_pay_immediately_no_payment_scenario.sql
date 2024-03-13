/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.FullAdmit.Claimant', '{"Notice.AAA7.DefResponse.FullAdmit.PayImmediately.Claimant"}', '{"Notice.AAA7.ClaimantIntent.FullAdmit.Claimant": ["respondent1PartyName", "defendantAdmittedAmountPaymentDeadlineEn", "defendantAdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.FullAdmit.Claimant', 'Immediate payment', 'Immediate payment',
        '<p class="govuk-body">You have accepted ${respondent1PartyName}''s plan to pay £{fullAdmitPayImmediatelyPaymentAmount} immediately. Funds must clear your account by ${defendantAdmittedAmountPaymentDeadlineEn}.</p><p class="govuk-body">If you don''t receive the money by then, you can <a href="{COUNTY_COURT_JUDGEMENT_URL}"  rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a>.</p>',
        '<p class="govuk-body">You have accepted ${respondent1PartyName}''s plan to pay £{fullAdmitPayImmediatelyPaymentAmount} immediately. Funds must clear your account by ${defendantAdmittedAmountPaymentDeadlineCy}.</p><p class="govuk-body">If you don''t receive the money by then, you can <a href="{COUNTY_COURT_JUDGEMENT_URL}"  rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a>.</p>',
        'CLAIMANT');
