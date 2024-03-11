/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant',
        '{"Notice.AAA7.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA7.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant",
        "Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
        "Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant"}',
        '{"Notice.AAA7.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant": ["claimantSettlementAgreement", "respondent1SettlementAgreementDeadline_En", "respondent1SettlementAgreementDeadline_Cy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant', 'Settlement agreement', 'Settlement agreement',
        '<p class="govuk-body">You have ${claimantSettlementAgreement} the defendant''s plan and asked them to sign a settlement agreement.</p><p class="govuk-body">The defendant must respond by ${respondent1SettlementAgreementDeadline_En}.</p><p class="govuk-body">If they do not respond by then, or reject the agreement, you can request a County Court Judgment.</p>',
        '<p class="govuk-body">You have ${claimantSettlementAgreement} the defendant''s plan and asked them to sign a settlement agreement.</p><p class="govuk-body">The defendant must respond by ${respondent1SettlementAgreementDeadline_Cy}.</p><p class="govuk-body">If they do not respond by then, or reject the agreement, you can request a County Court Judgment.</p>',
        'CLAIMANT');
