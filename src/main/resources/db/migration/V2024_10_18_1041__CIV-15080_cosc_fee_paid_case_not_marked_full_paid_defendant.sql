/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ProofofDebtPayment.Application.Defendant',
        '{}',
        '{"Notice.AAA6.ProofofDebtPayment.Application.Defendant":[]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ProofofDebtPayment.Application.Defendant',
        'Awaiting claimant confirmation',
        'Aros am gadarnhad yr hawlydd',
        '<p class="govuk-body">We''ve received your application to confirm you’ve paid a judgment debt. The person or business you owe money to now has a month to respond.</p>',
        '<p class="govuk-body">Rydym wedi cael eich cais i gadarnhau eich bod wedi talu dyled ddyfarniad. Mae gan yr unigolyn neu''r busnes y mae arnoch arian iddynt nawr fis i ymateb.</p>',
        'DEFENDANT');
