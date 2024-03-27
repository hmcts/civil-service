/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant","Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant":["respondent1SettlementAgreementDeadlineEn", "respondent1SettlementAgreementDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant',
        'Settlement agreement', 'Settlement agreement',
        '<p class="govuk-body">The claimant has rejected your plan and asked you to sign a settlement agreement.</p><p class="govuk-body">The claimant proposed a repayment plan, and the court then responded with an alternative plan that was accepted.</p><p class="govuk-body"> You must respond by ${respondent1SettlementAgreementDeadlineEn}. If you do not respond by then, or reject the agreement, they can request a County Court Judgment.</p><p class="govuk-body"><a href="{VIEW_REPAYMENT_PLAN}"  rel="noopener noreferrer" class="govuk-link">View the repayment plan</a><br><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View your response</a></p>',
        '<p class="govuk-body">The claimant has rejected your plan and asked you to sign a settlement agreement.</p><p class="govuk-body">The claimant proposed a repayment plan, and the court then responded with an alternative plan that was accepted.</p><p class="govuk-body"> You must respond by ${respondent1SettlementAgreementDeadlineCy}. If you do not respond by then, or reject the agreement, they can request a County Court Judgment.</p><p class="govuk-body"><a href="{VIEW_REPAYMENT_PLAN}"  rel="noopener noreferrer" class="govuk-link">View the repayment plan</a><br><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View your response</a></p>',
        'DEFENDANT');

/**
 * Add task list items
 * No required
 */
