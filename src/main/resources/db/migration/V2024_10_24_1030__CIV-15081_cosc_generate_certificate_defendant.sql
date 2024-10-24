/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ProofofDebtPayment.ApplicationProcessed.Defendant',
        '{"Notice.AAA6.ProofofDebtPayment.Application.Defendant", "Notice.AAA6.ProofofDebtPayment.Application.Claimant"}',
        '{"Notice.AAA6.ProofofDebtPayment.ApplicationProcessed.Defendant":[]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ProofofDebtPayment.ApplicationProcessed.Defendant',
        'Proof of debt payment issued',
        'Cadarnhad o daliad dyled dyfarniad',
        '<p class="govuk-body">You’ve successfully confirmed that you’ve paid a judgment (CCJ) debt in full. The public register will be updated and you can now <a href="{VIEW_COSC_CERTIFICATE_URL}" class="govuk-link">view the certificate</a>. You should download this certificate for your records. The certificate can be found in ‘Orders and notices from the court’.</p>',
        '<p class="govuk-body">Rydych chi wedi cadarnhau’n llwyddiannus eich bod wedi talu dyled y dyfarniad (CCJ) yn llawn. Bydd y gofrestr gyhoeddus yn cael ei diweddaru a gallwch <a href="{VIEW_COSC_CERTIFICATE_URL}" class="govuk-link">nawr weld y dystysgrif</a>. Dylech lawrlwytho’r dystysgrif hon ar gyfer eich cofnodion. Gellir dod o hyd i’r dystysgrif yn ’Gorchmynion a hysbysiadau gan y llys’.</p>',
        'DEFENDANT');
