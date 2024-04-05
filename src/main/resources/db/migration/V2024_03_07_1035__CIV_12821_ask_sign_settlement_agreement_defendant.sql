/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant" : ["respondent1SettlementAgreementDeadlineEn", "respondent1SettlementAgreementDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant', 'Settlement agreement', 'Settlement agreement',
        '<p class="govuk-body">The claimant has accepted your plan and asked you to sign a settlement agreement. You must respond by ${respondent1SettlementAgreementDeadlineEn}.</p><p class="govuk-body">If you do not respond by then, or reject the agreement, they can request a County Court Judgment.</p><p class="govuk-body"><a href="{VIEW_REPAYMENT_PLAN}" rel="noopener noreferrer" class="govuk_link">View the repayment plan</a><br><a href="{VIEW_RESPONSE_TO_CLAIM}" rel="noopener noreferrer" class="govuk_link">View your response</a></p>',
        '<p class="govuk-body">The claimant has accepted your plan and asked you to sign a settlement agreement. You must respond by ${respondent1SettlementAgreementDeadlineCy}.</p><p class="govuk-body">If you do not respond by then, or reject the agreement, they can request a County Court Judgment.</p><p class="govuk-body"><a href="{VIEW_REPAYMENT_PLAN}" rel="noopener noreferrer" class="govuk_link">View the repayment plan</a><br><a href="{VIEW_RESPONSE_TO_CLAIM}" rel="noopener noreferrer" class="govuk_link">View your response</a></p>',
        'DEFENDANT');

