/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SettlementNoResponse.Claimant',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.SettlementNoResponse.Claimant": ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SettlementNoResponse.Claimant',
        '${respondent1PartyName} has not signed your settlement agreement',
        '${respondent1PartyName} has not signed your settlement agreement',
        '<p class="govuk-body">You can <a href="{COUNTY_COURT_JUDGEMENT_URL}" rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a> (CCJ), based on the repayment plan shown in the agreement.</p> <p class="govuk-body">The court will make an order requiring them to pay the money. The order does not guarantee that they will pay it.</p> <p class="govuk-body">${respondent1PartyName} can still sign the settlement agreement until you request a CCJ.</p>',
        '<p class="govuk-body">You can <a href="{COUNTY_COURT_JUDGEMENT_URL}" rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a> (CCJ), based on the repayment plan shown in the agreement.</p> <p class="govuk-body">The court will make an order requiring them to pay the money. The order does not guarantee that they will pay it.</p> <p class="govuk-body">${respondent1PartyName} can still sign the settlement agreement until you request a CCJ.</p>',
        'CLAIMANT');
