/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationUnsuccessful.NOTClaimant1NonContactable.CARM.Claimant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Claimant"}',
        '{"Notice.AAA6.MediationUnsuccessful.NOTClaimant1NonContactable.CARM.Claimant": []}');

/**
 * Add notification template for claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.MediationUnsuccessful.NOTClaimant1NonContactable.CARM.Claimant',
        'Mediation appointment unsuccessful',
        'Mediation appointment unsuccessful',
        '<p class="govuk-body">You were not able to resolve this claim using mediation. This case will now be reviewed by the court.</p>',
        '<p class="govuk-body">You were not able to resolve this claim using mediation. This case will now be reviewed by the court.</p>',
        'CLAIMANT');

/**
 * Add scenario for defendant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationUnsuccessful.NOTDefendant1NonContactable.CARM.Defendant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Defendant"}',
        '{"Notice.AAA6.MediationUnsuccessful.NOTDefendant1NonContactable.CARM.Defendant": []}');

/**
 * Add notification template from defendant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.MediationUnsuccessful.NOTDefendant1NonContactable.CARM.Defendant',
        'Mediation appointment unsuccessful',
        'Mediation appointment unsuccessful',
        '<p class="govuk-body">You were not able to resolve this claim using mediation. This case will now be reviewed by the court.</p>',
        '<p class="govuk-body">You were not able to resolve this claim using mediation. This case will now be reviewed by the court.</p>',
        'DEFENDANT');
