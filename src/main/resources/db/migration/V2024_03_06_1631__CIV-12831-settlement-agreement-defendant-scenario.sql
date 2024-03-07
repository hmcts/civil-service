/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant',
        '{"Notice.AAA7.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant", "Notice.AAA7.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant"}',
        '{"Notice.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant" : ["respondSettlementAgreementDeadline"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant',
        'Settlement agreement',
        'Settlement agreement',
        'The claimant has rejected your plan and asked you to sign a settlement agreement.<br><br>The claimant has proposed a new repayment plan and the court has agreed with it, based on the financial details you provided.<br><br>You must respond by ${respondSettlementAgreementDeadline}. If you do not respond by then, or reject the agreement, they can request a County Court Judgment.<br><br><a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">View the repayment plan</a>.<br><a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">View your response</a>.',
        'The claimant has rejected your plan and asked you to sign a settlement agreement.<br><br>The claimant has proposed a new repayment plan and the court has agreed with it, based on the financial details you provided.<br><br>You must respond by ${respondSettlementAgreementDeadline}. If you do not respond by then, or reject the agreement, they can request a County Court Judgment.<br><br><a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">View the repayment plan</a>.<br><a href="{RESPONSE_TASK_LIST_URL}"  rel="noopener noreferrer" class="govuk-link">View your response</a>.',
        'DEFENDANT');
