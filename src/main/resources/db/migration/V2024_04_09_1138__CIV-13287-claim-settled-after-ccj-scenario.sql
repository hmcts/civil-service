/**
 * Add claimant scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimantInformsClaimPaid.PostCCJ.Claimant','{"Notice.AAA6.ClaimantIntent.RequestedCCJ.Claimant",
        "Notice.AAA6.ClaimantIntent.RequestJudgePlan.RequestedCCJ.Claimant"}','{"Notice.AAA6.ClaimantIntent.ClaimantInformsClaimPaid.PostCCJ.Claimant" : ["respondent1PartyName", "claimSettledDateEn", "claimSettledDateCy"]}');

/**
 * Add claimant notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimantInformsClaimPaid.PostCCJ.Claimant', 'Claim is settled', 'Claim is settled',
        '<p class="govuk-body">${respondent1PartyName} paid you on ${claimSettledDateEn}.</p><p class="govuk-body">If the defendant paid you within 28 days of the judgment being issued then the defendant''s County Court Judgment will be cancelled.</p><p class="govuk-body">If the defendant paid you after this, then the judgment will be marked as paid.</p>',
        '<p class="govuk-body">${respondent1PartyName} paid you on ${claimSettledDateCy}.</p><p class="govuk-body">If the defendant paid you within 28 days of the judgment being issued then the defendant''s County Court Judgment will be cancelled.</p><p class="govuk-body">If the defendant paid you after this, then the judgment will be marked as paid.</p>',
        'CLAIMANT');

/**
 * Add defendant scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimantInformsClaimPaid.PostCCJ.Defendant','{"Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Defendant",
        "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantRejectsDefPlan.CourtAgreesWithClaimant.Defendant",
        "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantRejectsDefPlan.CourtAgreesWithDef.Defendant",
        "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantRejectsDefPlan.ClaimantDisagreesCourtPlan.Defendant",
        "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant",
        "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant"}','{"Notice.AAA6.ClaimantIntent.ClaimantInformsClaimPaid.PostCCJ.Defendant" : ["applicant1PartyName", "claimSettledDateEn", "claimSettledDateCy"]}');

/**
 * Add defendant notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimantInformsClaimPaid.PostCCJ.Defendant', 'Claim is settled', 'Claim is settled',
        '<p class="govuk-body">${applicant1PartyName} confirmed you settled on ${claimSettledDateEn}. This claim is now settled. If you need proof that the County Court Judgment (CCJ) is paid you can <a href="{APPLY_FOR_CERTIFICATE}" target="_blank" rel="noopener noreferrer" class="govuk-link">contact us to get a certificate of satisfaction</a>. This costs £15.</p>'
        '<p class="govuk-body">If you paid within 28 days of the judgment being issued, we''ll tell the Registry Trust to remove your CCJ from the register of judgments. The CCJ will not appear in any credit agency searches, though some agencies may not update their records immediately.</p>'
        '<p class="govuk-body">If you paid after 28 days of the judgment being issued, we''ll tell the Registry Trust to mark your CCJ as paid on the register of judgments. Any credit agency that checks the register will see that you''ve paid, though some may not update records immediately. It will remain on the register for 6 years, but it''ll be marked as ''satisfied''. <a href="{DOWNLOAD_DEFENDANT_RESPONSE}" target="_blank" rel="noopener noreferrer" class="govuk-link">Download your response</a>.</p>',
        '<p class="govuk-body">${applicant1PartyName} confirmed you settled on ${claimSettledDateCy}. This claim is now settled. If you need proof that the County Court Judgment (CCJ) is paid you can <a href="{APPLY_FOR_CERTIFICATE}" target="_blank" rel="noopener noreferrer" class="govuk-link">contact us to get a certificate of satisfaction</a>. This costs £15.</p>'
        '<p class="govuk-body">If you paid within 28 days of the judgment being issued, we''ll tell the Registry Trust to remove your CCJ from the register of judgments. The CCJ will not appear in any credit agency searches, though some agencies may not update their records immediately.</p>'
        '<p class="govuk-body">If you paid after 28 days of the judgment being issued, we''ll tell the Registry Trust to mark your CCJ as paid on the register of judgments. Any credit agency that checks the register will see that you''ve paid, though some may not update records immediately. It will remain on the register for 6 years, but it''ll be marked as ''satisfied''. <a href="{DOWNLOAD_DEFENDANT_RESPONSE}" target="_blank" rel="noopener noreferrer" class="govuk-link">Download your response</a>.</p>',
        'DEFENDANT');
