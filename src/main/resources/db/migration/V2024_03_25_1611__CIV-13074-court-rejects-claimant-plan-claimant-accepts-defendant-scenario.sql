/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.RequestCCJ.ClaimantRejectsDefPlan.CourtAgreesWithDef.Defendant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant" }',
        '{"Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantRejectsDefPlan.CourtAgreesWithDef.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantRejectsDefPlan.CourtAgreesWithDef.Defendant',
        '${applicant1PartyName} has requested a County Court Judgment against you', '${applicant1PartyName} has requested a County Court Judgment against you',
        '<p class="govuk-body">${applicant1PartyName} rejected your <a href={CCJ_REPAYMENT_PLAN_DEFENDANT_URL} class="govuk-link">repayment plan</a> and proposed a new plan. The court then responded with an alternative plan that was accepted in favour of you.</p>'
          '<p class="govuk-body">When we''ve processed the request, we''ll post a copy of the judgment to you.</p>'
          '<p class="govuk-body">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) is removed from the public register. You can pay £15 to <a href="{APPLY_FOR_CERTIFICATE}" rel="noopener noreferrer" class="govuk-link" target="_blank">apply for a certificate (opens in new tab)</a> that confirms this.</p>'
          '<p><a href="{CITIZEN_CONTACT_THEM_URL}" class="govuk-link">Contact ${applicant1PartyName}</a> if you need their payment details.<br><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a>.</p>',
        '<p class="govuk-body">${applicant1PartyName} rejected your <a href={CCJ_REPAYMENT_PLAN_DEFENDANT_URL} class="govuk-link">repayment plan</a> and proposed a new plan. The court then responded with an alternative plan that was accepted in favour of you.</p>'
          '<p class="govuk-body">When we''ve processed the request, we''ll post a copy of the judgment to you.</p>'
          '<p class="govuk-body">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) is removed from the public register. You can pay £15 to <a href="{APPLY_FOR_CERTIFICATE}" rel="noopener noreferrer" class="govuk-link" target="_blank">apply for a certificate (opens in new tab)</a> that confirms this.</p>'
          '<p><a href="{CITIZEN_CONTACT_THEM_URL}" class="govuk-link">Contact ${applicant1PartyName}</a> if you need their payment details.<br><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a>.</p>',
        'DEFENDANT');
