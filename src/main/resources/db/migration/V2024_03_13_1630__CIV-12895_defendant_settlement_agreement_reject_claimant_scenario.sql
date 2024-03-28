/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant', '{Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant}',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant" : ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant', 'Settlement agreement', 'Settlement agreement',
        '<p class="govuk-body">${respondent1PartyName} has rejected the settlement agreement.</p><p class="govuk-body">You can  <a href={REQUEST_CCJ_URL} class="govuk-link"> request a County Court Judgment</a> </p>',
        '<p class="govuk-body">${respondent1PartyName} has rejected the settlement agreement.</p><p class="govuk-body">You can  <a href={REQUEST_CCJ_URL} class="govuk-link"> request a County Court Judgment</a> </p>',
        'CLAIMANT');
