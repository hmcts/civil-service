/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant',
        '{"Notice.AAA7.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant","Notice.AAA7.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant"}',
        '{"Notice.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant":["responseDeadline"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant',
        'Settlement agreement', 'Settlement agreement',
        'The claimant has rejected your plan and asked you to sign a settlement agreement.The claimant proposed a repayment plan, and the court then responded with an alternative plan that was accepted. You must respond by ${responseDeadline}. If you do not respond by then, or reject the agreement, they can request a County Court Judgment.  a href={CCJ_REPAYMENT_PLAN_DEFENDANT_URL}  rel="noopener noreferrer" class="govuk-link"> View the repayment plan</a> <a href={DEFENDANT_RESPONSE} rel="noopener noreferrer" class="govuk-link"> View your response</a>',
        'The claimant has rejected your plan and asked you to sign a settlement agreement.The claimant proposed a repayment plan, and the court then responded with an alternative plan that was accepted. You must respond by ${responseDeadline}. If you do not respond by then, or reject the agreement, they can request a County Court Judgment.  a href={CCJ_REPAYMENT_PLAN_DEFENDANT_URL}  rel="noopener noreferrer" class="govuk-link"> View the repayment plan</a> <a href={DEFENDANT_RESPONSE} rel="noopener noreferrer" class="govuk-link"> View your response</a>',
        'DEFENDANT');

/**
 * Add task list items
 * No required
 */
