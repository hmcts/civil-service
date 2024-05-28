/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationSuccessful.CARM.Claimant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Claimant"}',
        '{"Notice.AAA6.MediationSuccessful.CARM.Claimant": []}');

/**
 * Add notification template for claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.MediationSuccessful.CARM.Claimant',
        'Mediation appointment successful',
        'Mediation appointment successful',
        '<p class="govuk-body">Both parties attended mediation and an agreement was reached. This case is now settled and no further action is needed. You can view your mediation agreement <a href="{MEDIATION_SUCCESSFUL_URL}" rel="noopener noreferrer" class="govuk-link" target="_blank">here</a>.</p>',
        '<p class="govuk-body">Both parties attended mediation and an agreement was reached. This case is now settled and no further action is needed. You can view your mediation agreement <a href="{MEDIATION_SUCCESSFUL_URL}" rel="noopener noreferrer" class="govuk-link" target="_blank">here</a>.</p>',
        'CLAIMANT');

/**
 * Add scenario for defendant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationSuccessful.CARM.Defendant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Defendant"}',
        '{"Notice.AAA6.MediationSuccessful.CARM.Defendant" : []}');

/**
 * Add notification template from defendant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.MediationSuccessful.CARM.Defendant',
        'Mediation appointment successful',
        'Mediation appointment successful',
        '<p class="govuk-body">Both parties attended mediation and an agreement was reached. This case is now settled and no further action is needed. You can view your mediation agreement <a href="{MEDIATION_SUCCESSFUL_URL}" rel="noopener noreferrer" class="govuk-link" target="_blank">here</a>.</p>',
        '<p class="govuk-body">Both parties attended mediation and an agreement was reached. This case is now settled and no further action is needed. You can view your mediation agreement <a href="{MEDIATION_SUCCESSFUL_URL}" rel="noopener noreferrer" class="govuk-link" target="_blank">here</a>.</p>',
        'DEFENDANT');
