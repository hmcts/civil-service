/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant",
        "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
        "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant": ["claimantSettlementAgreement", "respondent1SettlementAgreementDeadlineEn", "respondent1SettlementAgreementDeadlineCy","respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant', 'Settlement agreement', 'Settlement agreement',
        '<p class="govuk-body">You have ${claimantSettlementAgreement} the ${respondent1PartyName} offer and asked them to sign a settlement agreement.</p><p class="govuk-body">The defendant must respond by ${respondent1SettlementAgreementDeadlineEn}.</p><p class="govuk-body">If they do not respond by then, or reject the agreement, you can request a County Court Judgment(CCJ).</p>',
        '<p class="govuk-body">You have ${claimantSettlementAgreement} the ${respondent1PartyName} offer and asked them to sign a settlement agreement.</p><p class="govuk-body">The defendant must respond by ${respondent1SettlementAgreementDeadlineCy}.</p><p class="govuk-body">If they do not respond by then, or reject the agreement, you can request a County Court Judgment(CCJ).</p>',
        'CLAIMANT');
