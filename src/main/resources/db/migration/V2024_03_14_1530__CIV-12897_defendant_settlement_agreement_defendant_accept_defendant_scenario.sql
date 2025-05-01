/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SettlementAgreement.DefendantAccepted.Defendant', '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant","Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant","Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantAccepted.Defendant" : [""]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantAccepted.Defendant', 'Settlement agreement', 'Cytundeb setlo',
        '<p class="govuk-body">You have accepted the <a href={VIEW_SETTLEMENT_AGREEMENT} target="_blank" class="govuk-link"> settlement agreement (opens in a new tab)</a>. The claimant cannot request a County Court Judgment (CCJ), unless you break the terms of the agreement.</p>',
        '<p class="govuk-body">Rydych wedi derbyn y <a href={VIEW_SETTLEMENT_AGREEMENT} target="_blank" class="govuk-link"> cytundeb setlo (yn agor mewn tab newydd)</a>. Ni all yr hawlydd wneud cais am Ddyfarniad Llys Sirol (CCJ) oni bai eich bod yn torri telerau’r cytundeb.</p>',
        'DEFENDANT');
