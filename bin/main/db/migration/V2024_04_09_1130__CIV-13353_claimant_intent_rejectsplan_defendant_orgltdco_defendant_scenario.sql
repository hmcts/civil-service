/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimantRejectsPlan.DefendantOrgLtdCo.Defendant',
        '{"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.ClaimantRejectsPlan.DefendantOrgLtdCo.Defendant":["legacyCaseReference", "applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimantRejectsPlan.DefendantOrgLtdCo.Defendant',
        'The court will review the details and issue a judgment',
        'Bydd y llys yn adolygu’r manylion ac yn cyhoeddi dyfarniad',
        '<p class="govuk-body">${applicant1PartyName} has rejected your payment plan, the court will issue a County Court Judgment (CCJ). If you do not agree with the judgment, you can send in your financial details and ask for this to be redetermined.<br>Your online account will not be updated - any further updates will be by post.<br>Email the details and your claim number ${legacyCaseReference} to {cmcCourtEmailId} or send by post to: </p><p class="govuk-body">{cmcCourtAddress}</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} wedi gwrthod eich cynllun talu, a bydd y llys yn cyhoeddi Dyfarniad Llys Sirol (CCJ). Os nad ydych yn cytuno â’r dyfarniad, gallwch anfon eich manylion ariannol i’r llys a gofyn am ailbenderfyniad.<br>Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru - bydd unrhyw ddiweddariadau pellach yn cael eu hanfon drwy’r post.<br>Anfonwch y manylion a rhif eich hawliad ${legacyCaseReference} ar e-bost i {cmcCourtEmailId} neu postiwch yr wybodaeth i: </p><p class="govuk-body">{cmcCourtAddress}</p>',
        'DEFENDANT');
