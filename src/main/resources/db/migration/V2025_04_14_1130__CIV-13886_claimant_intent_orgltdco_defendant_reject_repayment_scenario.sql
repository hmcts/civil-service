/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.Defendant.OrgLtdCo.JO.Claimant',
        '{"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.Defendant.OrgLtdCo.JO.Claimant":["legacyCaseReference"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.Defendant.OrgLtdCo.JO.Claimant',
        'The court will review the details and issue a judgment',
        'Bydd y llys yn adolygu’r manylion ac yn cyhoeddi dyfarniad',
        '<p class="govuk-body">You have rejected the defendant''s payment plan, the court will issue a County Court Judgment (CCJ). If you do not agree with the judgment, you can send in the defendant''s financial details and ask for this to be redetermined.</p><p class="govuk-body">Email the details and your claim number ${legacyCaseReference} to {cmcCourtEmailId} or send by post to: </p><br>{cmcCourtAddress}',
        '<p class="govuk-body">Rydych wedi gwrthod cynllun talu’r diffynnydd a bydd y llys yn cyhoeddi Dyfarniad Llys Sirol (CCJ). Os nad ydych yn cytuno â’r dyfarniad, gallwch anfon manylion ariannol y diffynnydd i’r llys a gofyn am ailbenderfyniad.</p><p class="govuk-body">Anfonwch y manylion a rhif eich hawliad ${legacyCaseReference} ar e-bost i {cmcCourtEmailId} neu postiwch yr wybodaeth i: </p><br>{cmcCourtAddress}',
        'CLAIMANT');
