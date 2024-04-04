/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant',
        '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantAccepted.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant":["applicant1PartyName","claimantSettlementAgreement"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant',
        '${applicant1PartyName} has requested a County Court Judgment against you',
        '${applicant1PartyName} has requested a County Court Judgment against you',
        '<p class="govuk-body">The claimant ${claimantSettlementAgreement} your repayment plan and asked you to sign a settlement. You signed the agreement but the claimant says you have broken the terms.</p><p class="govuk-body">When we’ve processed the request, we’ll post a copy of the judgment to you.</p> <p class="govuk-body">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) is removed from the public register. You can pay £15 to <a href="{APPLY_FOR_CERTIFICATE}" target="_blank" rel="noopener noreferrer" class="govuk-link">apply for a certificate (opens in new tab)</a> that confirms this.<br><a href="{CITIZEN_CONTACT_THEM_URL}"  rel="noopener noreferrer" class="govuk-link">Contact ${applicant1PartyName}</a> if you need their payment details. <br> <a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View your response</a></p>',
        '<p class="govuk-body">The claimant ${claimantSettlementAgreement} your repayment plan and asked you to sign a settlement. You signed the agreement but the claimant says you have broken the terms.</p><p class="govuk-body">When we’ve processed the request, we’ll post a copy of the judgment to you.</p> <p class="govuk-body">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) is removed from the public register. You can pay £15 to <a href="{APPLY_FOR_CERTIFICATE}" target="_blank" rel="noopener noreferrer" class="govuk-link">apply for a certificate (opens in new tab)</a> that confirms this.<br><a href="{CITIZEN_CONTACT_THEM_URL}"  rel="noopener noreferrer" class="govuk-link">Contact ${applicant1PartyName}</a> if you need their payment details. <br> <a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View your response</a></p>',
        'DEFENDANT');
