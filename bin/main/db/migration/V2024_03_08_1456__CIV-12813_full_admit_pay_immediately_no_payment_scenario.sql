/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.FullAdmit.Claimant', '{"Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant"}', '{"Notice.AAA6.ClaimantIntent.FullAdmit.Claimant": ["respondent1PartyName", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.FullAdmit.Claimant', 'Immediate payment', 'Talu ar unwaith',
        '<p class="govuk-body">You have accepted ${respondent1PartyName}''s offer to pay £{fullAdmitPayImmediatelyPaymentAmount} immediately. Funds must be received in your account by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p><p class="govuk-body">If you don''t receive the money by then, <a href="{COUNTY_COURT_JUDGEMENT_URL}"  rel="noopener noreferrer" class="govuk-link">you can request a County Court Judgment(CCJ)</a>.</p>',
        '<p class="govuk-body">Rydych wedi derbyn cynnig ${respondent1PartyName} i dalu £{fullAdmitPayImmediatelyPaymentAmount} ar unwaith. Rhaid i’r arian gyrraedd eich cyfrif erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}.</p><p class="govuk-body">Os na fyddwch wedi cael yr arian erbyn hynny, <a href="{COUNTY_COURT_JUDGEMENT_URL}"  rel="noopener noreferrer" class="govuk-link">gallwch wneud cais am Ddyfarniad Llys Sifil (CCJ)</a>.</p>',
        'CLAIMANT');
