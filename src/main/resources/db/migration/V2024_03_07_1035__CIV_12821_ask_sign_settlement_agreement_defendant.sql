/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant" : ["respondent1SettlementAgreementDeadlineEn", "respondent1SettlementAgreementDeadlineCy","applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant', 'Settlement agreement', 'Cytundeb setlo',
        '<p class="govuk-body">${applicant1PartyName} has accepted your offer and asked you to sign a settlement agreement. You must respond by ${respondent1SettlementAgreementDeadlineEn}.</p><p class="govuk-body">If you do not respond by then, or reject the agreement, they can request a County Court Judgment (CCJ).</p><p class="govuk-body">You can <a href="{VIEW_REPAYMENT_PLAN}" rel="noopener noreferrer" class="govuk-link">view the repayment plan</a> or <a href="{VIEW_RESPONSE_TO_CLAIM}" rel="noopener noreferrer" class="govuk-link">view your response</a>.</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} wedi derbyn eich cynnig ac wedi gofyn i chi lofnodi cytundeb setlo. Mae’n rhaid i chi ymateb erbyn ${respondent1SettlementAgreementDeadlineCy}.</p><p class="govuk-body">Os na fyddwch wedi ymateb erbyn hynny, neu os byddwch yn gwrthod y cytundeb, gallant wneud cais am Ddyfarniad Llys Sifil (CCJ).</p><p class="govuk-body">Gallwch <a href="{VIEW_REPAYMENT_PLAN}" rel="noopener noreferrer" class="govuk-link">weld y cynllun ad-dalu</a> neu <a href="{VIEW_RESPONSE_TO_CLAIM}" rel="noopener noreferrer" class="govuk-link">weld eich ymateb</a>.</p>',
        'DEFENDANT');

