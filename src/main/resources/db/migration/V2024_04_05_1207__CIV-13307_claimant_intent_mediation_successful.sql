/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.MediationSuccessful.Claimant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.MediationSuccessful.Claimant" : ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.MediationSuccessful.Claimant', 'You settled the claim through mediation', 'You settled the claim through mediation',
        '<p class="govuk-body">You made an agreement which means the claim is now ended and sets out the terms of how ${respondent1PartyName} must repay you.</p><p class="govuk-body"><a href="{MEDIATION_SUCCESSFUL_URL}" rel="noopener noreferrer" class="govuk-link" target="_blank">Download the agreement</a></p>',
        '<p class="govuk-body">You made an agreement which means the claim is now ended and sets out the terms of how ${respondent1PartyName} must repay you.</p><p class="govuk-body"><a href="{MEDIATION_SUCCESSFUL_URL}" rel="noopener noreferrer" class="govuk-link" target="_blank">Download the agreement</a></p>',
        'CLAIMANT');
