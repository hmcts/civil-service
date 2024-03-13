INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.ClaimSettled.Claimant',
        '{"Notice.AAA7.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA7.DefResponse.Full Defence.AlreadyPaid.Claimant"}', '{"Notice.AAA7.ClaimantIntent.ClaimSettled.Claimant":["claimSettledAmount", "claimSettledDateEn", "claimSettledDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.ClaimSettled.Claimant', 'The claim is settled', 'The claim is settled',
        'You have confirmed that the defendant paid ${claimSettledAmount} on ${claimSettledDateEn}.',
        'You have confirmed that the defendant paid ${claimSettledAmount} on ${claimSettledDateCy}.',
        'CLAIMANT');
