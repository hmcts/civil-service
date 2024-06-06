/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.MediationUnsuccessful.Claimant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Claimant" : ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Claimant',
        'Mediation was unsuccessful',
        'Nid oedd y cyfryngu yn llwyddiannus',
        '<p class="govuk-body">You weren''t able to resolve your claim against ${respondent1PartyName} using mediation. The court will review the case. We''ll contact you to tell you what to do next.</p>',
        '<p class="govuk-body">Nid oeddech yn gallu datrys eich hawliad yn erbyn ${respondent1PartyName} drwy gyfryngu. Bydd y llys yn adolygu’r achos. Byddwn yn cysylltu â chi i ddweud wrthych beth i’w wneud nesaf.</p>',
        'CLAIMANT');
