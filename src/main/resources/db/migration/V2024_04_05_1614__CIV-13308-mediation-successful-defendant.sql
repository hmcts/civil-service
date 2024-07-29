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
        'Rydych wedi setlo’r hawliad drwy gyfryngu',
        '<p class="govuk-body">You made an agreement which means the claim is now ended and sets out the terms of how you must repay ${applicant1PartyName}.</p> <p class="govuk-body"><a href="{MEDIATION_SUCCESSFUL_URL}" target="_blank" rel="noopener noreferrer" class="govuk-link">Download the agreement (opens in a new tab)</a></p> <p class="govuk-body"><a href="{CITIZEN_CONTACT_THEM_URL}" rel="noopener noreferrer" class="govuk-link">Contact ${applicant1PartyName}</a> if you need their payment details. Make sure you get receipts for any payments.</p>',
        '<p class="govuk-body">Mi wnaethoch gytundeb sy’n golygu bod yr hawliad nawr ar ben. Mae’r cytundeb yn nodi’r telerau ar gyfer sut mae rhaid i chi ad-dalu ${applicant1PartyName}.</p> <p class="govuk-body"><a href="{MEDIATION_SUCCESSFUL_URL}" target="_blank" rel="noopener noreferrer" class="govuk-link">Lawrlwytho’r cytundeb (yn agor mewn tab newydd)</a></p> <p class="govuk-body"><a href="{CITIZEN_CONTACT_THEM_URL}" rel="noopener noreferrer" class="govuk-link">Cysylltwch â ${applicant1PartyName}</a> ios oes arnoch angen eu manylion talu. Gwnewch yn siŵr eich bod yn cael derbynebau am unrhyw daliadau.</p>',
        'DEFENDANT');

