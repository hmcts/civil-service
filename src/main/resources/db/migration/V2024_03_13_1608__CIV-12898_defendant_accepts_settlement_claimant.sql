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
        'Cytundeb setlo',
        '<p class="govuk-body">${respondent1PartyName} has accepted the settlement agreement. You cannot <a href="{COUNTY_COURT_JUDGEMENT_URL}" rel="noopener noreferrer" class="govuk-link">request a County Court Judgment(CCJ)</a>,  unless they break the terms of the agreement.</p> <p class="govuk-body">You can <a href="{DOWNLOAD_SETTLEMENT_AGREEMENT}" target="_blank" rel="noopener noreferrer" class="govuk-link">view the settlement agreement (opens in a new tab)</a> or <a href="{TELL_US_IT_IS_SETTLED}" rel="noopener noreferrer" class="govuk-link">tell us it''s settled</a>.</p>',
        '<p class="govuk-body">Mae ${respondent1PartyName} wedi derbyn y cytundeb setlo. Ni allwch <a href="{COUNTY_COURT_JUDGEMENT_URL}" rel="noopener noreferrer" class="govuk-link">wneud cais am Ddyfarniad Llys Sirol(CCJ)</a>,  oni bai eu bod yn torri telerau’r cytundeb.</p> <p class="govuk-body">Gallwch <a href="{DOWNLOAD_SETTLEMENT_AGREEMENT}" target="_blank" rel="noopener noreferrer" class="govuk-link"> weld y cytundeb setlo (yn agor mewn tab newydd)</a> neu <a href="{TELL_US_IT_IS_SETTLED}" rel="noopener noreferrer" class="govuk-link">ddweud wrthym ei fod wedi’i setlo</a>.</p>',
        'CLAIMANT');
