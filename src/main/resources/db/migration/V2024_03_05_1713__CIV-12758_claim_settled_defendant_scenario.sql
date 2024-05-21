/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimSettled.Defendant', '{"Notice.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.ClaimSettled.Defendant" : ["claimSettledAmount","claimSettledDateEn","claimSettledDateCy","applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimSettled.Defendant', 'The claim is settled', 'The claim is settled',
        '<p class="govuk-body">${applicant1PartyName} has confirmed that you paid ${claimSettledAmount} on ${claimSettledDateEn}.</p>',
        '<p class="govuk-body">${applicant1PartyName} has confirmed that you paid ${claimSettledAmount} on ${claimSettledDateCy}.</p>',
        'DEFENDANT');
