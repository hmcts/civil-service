/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.FullAdmit.Claimant', '{"Notice.AAA7.DefResponse.FullAdmit.PayImmediately.Claimant"}', '{"Notice.AAA7.ClaimantIntent.FullAdmit.Claimant":["defendantName", "fullAdmitAmount", "payInFullByDate"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.FullAdmit.Claimant', 'Immediate payment', 'Immediate payment',
        'You have accepted ${defendantName}''s plan to pay ${fullAdmitAmount} immediately. Funds must clear your account by ${payInFullByDate}. If you don''t receive the money by then, you can request a County Court Judgment.',
        'You have accepted ${defendantName}''s plan to pay ${fullAdmitAmount} immediately. Funds must clear your account by ${payInFullByDate}. If you don''t receive the money by then, you can request a County Court Judgment.',
        'CLAIMANT');

