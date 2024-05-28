/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.MediationSuccessful.Defendant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.MediationSuccessful.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.MediationSuccessful.Defendant', 'You settled the claim through mediation',
        'You settled the claim through mediation',
        '<p class="govuk-body">You made an agreement which means the claim is now ended and sets out the terms of how you must repay ${applicant1PartyName}.</p> <p class="govuk-body"><a href="{MEDIATION_SUCCESSFUL_URL}" target="_blank" rel="noopener noreferrer" class="govuk-link">Download the agreement</a></p> <p class="govuk-body"><a href="{CITIZEN_CONTACT_THEM_URL}" rel="noopener noreferrer" class="govuk-link">Contact ${applicant1PartyName}</a> if you need their payment details. Make sure you get receipts for any payments.</p>',
        '<p class="govuk-body">You made an agreement which means the claim is now ended and sets out the terms of how you must repay ${applicant1PartyName}.</p> <p class="govuk-body"><a href="{MEDIATION_SUCCESSFUL_URL}" target="_blank" rel="noopener noreferrer" class="govuk-link">Download the agreement</a></p> <p class="govuk-body"><a href="{CITIZEN_CONTACT_THEM_URL}" rel="noopener noreferrer" class="govuk-link">Contact ${applicant1PartyName}</a> if you need their payment details. Make sure you get receipts for any payments.</p>',
        'DEFENDANT');

