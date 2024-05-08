INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimSettled.Claimant',
        '{"Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.Full Defence.AlreadyPaid.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.ClaimSettled.Claimant":["respondent1PartyName","claimSettledAmount", "claimSettledDateEn", "claimSettledDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimSettled.Claimant', 'The claim is settled', 'Mae’r hawliad wedi’i setlo',
        '<p class="govuk-body">You have confirmed that ${respondent1PartyName} paid ${claimSettledAmount} on ${claimSettledDateEn}.</p>',
        '<p class="govuk-body">Rydych wedi cadarnhau bod ${respondent1PartyName} wedi talu ${claimSettledAmount} ar ${claimSettledDateCy}.</p>',
        'CLAIMANT');
