/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimSettled.Defendant', '{"Notice.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.ClaimSettled.Defendant" : ["claimSettledDateEn","claimSettledDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimSettled.Defendant', 'The claim is settled', 'Mae’r hawliad wedi’i setlo',
        '<p class="govuk-body">The claimant has confirmed that this case was settled on ${claimSettledDateEn}.</p>'
          '<p class="govuk-body">If you do not agree that the case is settled, please outline your objections in writing within 19 days of the settlement date, to the Civil National Business Centre using the email address at {cmcCourtEmailId}</p>',
        '<p class="govuk-body">The claimant has confirmed that this case was settled on ${claimSettledDateEn}.</p>'
          '<p class="govuk-body">If you do not agree that the case is settled, please outline your objections in writing within 19 days of the settlement date, to the Civil National Business Centre using the email address at {cmcCourtEmailId}</p>',
        'DEFENDANT');
