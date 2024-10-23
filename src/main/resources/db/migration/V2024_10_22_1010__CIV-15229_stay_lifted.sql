/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Stay.Lifted.Claimant',
        '{"Notice.AAA6.CP.Case.Stayed.Claimant"}',
        '{"Notice.AAA6.CP.Stay.Lifted.Claimant": []}'),
       ('Scenario.AAA6.CP.Stay.Lifted.Defendant',
        '{"Notice.AAA6.CP.Case.Stayed.Defendant"}',
        '{"Notice.AAA6.CP.Stay.Lifted.Defendant": []}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.Stay.Lifted.Claimant',
        'The stay has been lifted',
        'Mae''r ataliad wedi''i godi',
        '<p class="govuk-body">The stay of these proceedings has been lifted.</p>',
        '<p class="govuk-body">Mae''r ataliad ar gyfer yr achos hwn wedi''i godi.</p>',
        'CLAIMANT'),
       ('Notice.AAA6.CP.Stay.Lifted.Defendant',
        'The stay has been lifted',
        'Mae''r ataliad wedi''i godi',
        '<p class="govuk-body">The stay of these proceedings has been lifted.</p>',
        '<p class="govuk-body">Mae''r ataliad ar gyfer yr achos hwn wedi''i godi.</p>',
        'DEFENDANT');
