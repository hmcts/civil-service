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
        'Awaiting claimant confirmation',
        '<p class="govuk-body">We''ve received your application for proof that you''ve paid your debt. The claimant will now have 30 days to confirm this. If they don''t respond in this time then the certificate will be issued automatically.</p>',
        '<p class="govuk-body">We''ve received your application for proof that you''ve paid your debt. The claimant will now have 30 days to confirm this. If they don''t respond in this time then the certificate will be issued automatically.</p>',
        'DEFENDANT');
