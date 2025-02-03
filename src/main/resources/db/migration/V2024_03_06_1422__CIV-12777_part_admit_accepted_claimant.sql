INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.PartAdmit.Claimant',
        '{"Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant"}', '{"Notice.AAA6.ClaimantIntent.PartAdmit.Claimant":["respondent1PartyName", "defendantAdmittedAmount", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.PartAdmit.Claimant', 'Immediate payment', 'Talu ar unwaith',
        '<p class="govuk-body">${respondent1PartyName} has said that they will pay you ${defendantAdmittedAmount} immediately in full and final settlement of your claim and you have accepted this offer. Funds must be received in your account by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p> <p class="govuk-body">If you don''t receive the money by then, you can <a href={COUNTY_COURT_JUDGEMENT_URL} class="govuk-link">request a County Court Judgment(CCJ)</a>.</p>',
        '<p class="govuk-body">Mae ${respondent1PartyName} wedi dweud y byddant yn talu ${defendantAdmittedAmount} i chi ar unwaith fel setliad llawn a therfynol o’ch hawliad ac rydych wedi derbyn y cynnig hwn. Rhaid i’r arian gyrraedd eich cyfrif erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}.</p> <p class="govuk-body">Os nad ydych wedi cael yr arian erbyn hynny, gallwch <a href={COUNTY_COURT_JUDGEMENT_URL} class="govuk-link">wneud cais am Ddyfarniad Llys Sifil (CCJ)</a>.</p>',
        'CLAIMANT');
