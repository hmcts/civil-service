/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant" : ["respondent1SettlementAgreementDeadlineEn", "respondent1SettlementAgreementDeadlineCy","applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant',
        'Settlement agreement',
        'Cytundeb setlo',
        '<p class="govuk-body">${applicant1PartyName} has rejected your offer and asked you to sign a settlement agreement.</p><p class="govuk-body">${applicant1PartyName} has proposed a new repayment plan and the court has agreed with it, based on the financial details you provided.</p><p class="govuk-body">You must respond by ${respondent1SettlementAgreementDeadlineEn}. If you do not respond by then, or reject the agreement, they can request a County Court Judgment (CCJ).</p><p class="govuk-body">You can <a href="{VIEW_REPAYMENT_PLAN}"  rel="noopener noreferrer" class="govuk-link">view the repayment plan</a> or <a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">view your response</a>.</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} wedi gwrthod eich cynnig ac wedi gofyn i chi lofnodi cytundeb setlo.</p><p class="govuk-body">Mae ${applicant1PartyName} wedi cynnig cynllun ad-dalu newydd, ac mae’r llys wedi cytuno iddo, yn seiliedig ar y manylion ariannol a ddarparwyd gennych.</p><p class="govuk-body">Mae’n rhaid i chi ymateb erbyn ${respondent1SettlementAgreementDeadlineCy}. Os na fyddwch wedi ymateb erbyn hynny, neu os byddwch yn gwrthod y cytundeb, gallant wneud cais am Ddyfarniad Llys Sifil (CCJ).</p><p class="govuk-body">Gallwch <a href="{VIEW_REPAYMENT_PLAN}"  rel="noopener noreferrer" class="govuk-link">weld y cynllun ad-dalu</a> neu <a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">weld eich ymateb</a>.</p>',
        'DEFENDANT');
