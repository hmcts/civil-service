/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Case.Stayed.Claimant',
        '{}',
        '{"Notice.AAA6.CP.Case.Stayed.Claimant" : []}'),
       ('Scenario.AAA6.CP.Case.Stayed.Defendant',
        '{}',
        '{"Notice.AAA6.CP.Case.Stayed.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.Case.Stayed.Claimant', 'The case has been stayed', 'Mae’r achos wedi cael ei atal',
        '<p class="govuk-body">The case has been stayed. This could be as a result of a judge’s order. Any upcoming hearings will be cancelled.</p>',
        '<p class="govuk-body">Mae’r achos wedi’i atal. Gallai hyn fod o ganlyniad i orchymyn a waned gan farnwr. Bydd unrhyw wrandawiadau sydd i ddod yn cael eu canslo.</p>',
        'CLAIMANT'),
       ('Notice.AAA6.CP.Case.Stayed.Defendant', 'The case has been stayed', 'Mae’r achos wedi cael ei atal',
        '<p class="govuk-body">The case has been stayed. This could be as a result of a judge’s order. Any upcoming hearings will be cancelled.</p>',
        '<p class="govuk-body">Mae’r achos wedi’i atal. Gallai hyn fod o ganlyniad i orchymyn a waned gan farnwr. Bydd unrhyw wrandawiadau sydd i ddod yn cael eu canslo.</p>',
        'DEFENDANT');

