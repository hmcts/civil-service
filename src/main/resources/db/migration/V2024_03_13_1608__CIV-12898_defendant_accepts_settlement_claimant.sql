/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.Settlement.DefendantResponseAccepts.Claimant',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant", "Notice.AAA6.ClaimIssue.Response.Await"}',
        '{"Notice.AAA6.ClaimantIntent.Settlement.DefendantResponseAccepts.Claimant": ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.Settlement.DefendantResponseAccepts.Claimant',
        'Settlement agreement',
        'Settlement agreement',
        '<p class="govuk-body">${respondent1PartyName} has accepted the settlement agreement.</p> <p class="govuk-body">You cannot <a href="{COUNTY_COURT_JUDGEMENT_URL}" rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a>,  unless they break the terms of the agreement.</p> <p class="govuk-body"> <a href="{DOWNLOAD_SETTLEMENT_AGREEMENT}" target="_blank" rel="noopener noreferrer" class="govuk-link">View the settlement agreement</a> <br> <a href="{TELL_US_IT_IS_SETTLED}" rel="noopener noreferrer" class="govuk-link">Tell us it''s settled</a></p>',
        '<p class="govuk-body">${respondent1PartyName} has accepted the settlement agreement.</p> <p class="govuk-body">You cannot <a href="{COUNTY_COURT_JUDGEMENT_URL}" rel="noopener noreferrer" class="govuk-link">request a County Court Judgment</a>,  unless they break the terms of the agreement.</p> <p class="govuk-body"> <a href="{DOWNLOAD_SETTLEMENT_AGREEMENT}" target="_blank" rel="noopener noreferrer" class="govuk-link">View the settlement agreement</a> <br> <a href="{TELL_US_IT_IS_SETTLED}" rel="noopener noreferrer" class="govuk-link">Tell us it''s settled</a></p>',
        'CLAIMANT');
