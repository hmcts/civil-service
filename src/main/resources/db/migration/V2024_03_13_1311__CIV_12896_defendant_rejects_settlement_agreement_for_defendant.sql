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
        '<p class="govuk-body">Gall ${applicant1PartyName} wneud cais am Ddyfarniad Llys Sirol (CCJ), a fyddai’n gorchymyn eich bod yn ad-dalu’r arian yn unol â’r cytundeb. Mae’r llys yn credu y gallwch fforddio hyn.</p> <p class="govuk-body"> Os bydd yr hawlydd yn gwneud cais am CCJ yna gallwch ofyn i farnwr ystyried newid y cynllun, yn seiliedig ar eich manylion ariannol.</p>',
        'DEFENDANT');

/**
 * Add task list items
 * No required
 */
