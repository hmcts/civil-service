/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.PartAdmit.Defendant', '{"Notice.AAA7.DefResponse.FullOrPartAdmit.PayImmediately.Defendant"}', '{"Notice.AAA7.ClaimantIntent.PartAdmit.Defendant":["defendantAdmittedAmount", "defendantAdmittedAmountPaymentDeadlineEn", "defendantAdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.PartAdmit.Defendant', 'Immediate payment', 'Immediate payment',
        '<p class="govuk-body">The claimant has accepted your plan to pay £${defendantAdmittedAmount} immediately. Funds must clear <a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">their account</a> by ${defendantAdmittedAmountPaymentDeadlineEn}. If they don´t receive the money by then, they can request a County Court Judgment.</p>',
        '<p class="govuk-body">The claimant has accepted your plan to pay £${defendantAdmittedAmount} immediately. Funds must clear <a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">their account</a> by ${defendantAdmittedAmountPaymentDeadlineCy}. If they don´t receive the money by then, they can request a County Court Judgment.</p>',
        'DEFENDANT');

/**
 * Add task list items
 * No required
 */
