INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.PartAdmit.Claimant',
        '{"Notice.AAA7.DefResponse.PartAdmit.PayImmediately.Claimant"}', '{"Notice.AAA7.ClaimantIntent.PartAdmit.Claimant":["defendantName", "claimSettledAmount", "payImmediatelyAmountDeadline"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.PartAdmit.Claimant', 'Immediate payment', 'Immediate payment',
        '${defendantName} said they will pay you ${claimSettledAmount} immediately. Funds must clear your account by ${payImmediatelyAmountDeadline}. If you don´t receive the money by then, you can <a href={COUNTY_COURT_JUDGEMENT_URL}  rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a>.',
        '${defendantName} said they will pay you ${claimSettledAmount} immediately. Funds must clear your account by ${payImmediatelyAmountDeadline}. If you don´t receive the money by then, you can <a href={COUNTY_COURT_JUDGEMENT_URL}  rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a>.',
        'CLAIMANT');
