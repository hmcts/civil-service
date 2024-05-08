/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Defendant',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Defendant":["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Defendant', 'Settlement agreement', 'Cytundeb setlo',
        '<p class="govuk-body">${applicant1PartyName} can request a County Court Judgment (CCJ), which would order you to repay the money in line with the agreement. The court believes you can afford this.</p> <p class="govuk-body">If the claimant requests a CCJ then you can ask a judge to consider changing the plan, based on your financial details.</p>',
        '<p class="govuk-body">${applicant1PartyName} can request a County Court Judgment (CCJ), which would order you to repay the money in line with the agreement. The court believes you can afford this.</p> <p class="govuk-body">If the claimant requests a CCJ then you can ask a judge to consider changing the plan, based on your financial details.</p>',
        'DEFENDANT');

/**
 * Add task list items
 * No required
 */
