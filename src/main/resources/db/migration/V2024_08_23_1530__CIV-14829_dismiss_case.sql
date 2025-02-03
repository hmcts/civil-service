/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Case.Dismissed.Claimant',
        '{}',
        '{"Notice.AAA6.CP.Case.Dismissed.Claimant": []}'),
       ('Scenario.AAA6.CP.Case.Dismissed.Defendant',
        '{}',
        '{"Notice.AAA6.CP.Case.Dismissed.Defendant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.Case.Dismissed.Claimant',
        'The case has been closed',
        'Mae’r achos wedi’i gau',
        '<p class="govuk-body">The case has been closed as a result of a judge’s order.<br>' ||
        'You can view the case summary but not the case details. You cannot make any changes to a closed case.</p>',
        '<p class="govuk-body">Mae’r achos wedi’i gau o ganlyniad i orchymyn  barnwr.<br>' ||
        'Gallwch weld y crynodeb o’r achos ond nid manylion yr achos. Ni allwch wneud unrhyw newidiadau i achos sydd wedi cau.</p>',
        'CLAIMANT', 'Session'),
       ('Notice.AAA6.CP.Case.Dismissed.Defendant',
        'The case has been closed',
        'Mae’r achos wedi’i gau',
        '<p class="govuk-body">The case has been closed as a result of a judge’s order.<br>' ||
        'You can view the case summary but not the case details. You cannot make any changes to a closed case.</p>',
        '<p class="govuk-body">Mae’r achos wedi’i gau o ganlyniad i orchymyn  barnwr.<br>' ||
        'Gallwch weld y crynodeb o’r achos ond nid manylion yr achos. Ni allwch wneud unrhyw newidiadau i achos sydd wedi cau.</p>',
        'DEFENDANT', 'Session');
