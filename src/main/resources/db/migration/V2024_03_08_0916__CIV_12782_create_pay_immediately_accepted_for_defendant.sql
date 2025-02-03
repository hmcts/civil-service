/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.PartAdmit.Defendant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.PartAdmit.Defendant":["defendantAdmittedAmount", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy","applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.PartAdmit.Defendant', 'Immediate payment', 'Talu ar unwaith',
        '<p class="govuk-body">${applicant1PartyName} has accepted your offer to pay ${defendantAdmittedAmount} immediately in full and final settlement of the claim. Funds must be received in <a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">their account</a> by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p><p class="govuk-body">If they don''t receive the money by then, they can request a County Court Judgment (CCJ).</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} wedi derbyn eich cynnig i dalu ${defendantAdmittedAmount} yn llawn ar unwaith fel setliad llawn a therfynol o’r hawliad.  Rhaid i’r arian fod yn <a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">ei g/chyfrif</a> erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}.</p><p class="govuk-body"> Os na fydd yr arian wedi cyrraedd erbyn hynny, gallant ofyn am Ddyfarniad Llys Sirol (CCJ).</p>',
        'DEFENDANT');

/**
 * Add task list items
 * No required
 */
