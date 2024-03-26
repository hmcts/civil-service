/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant',
        '{"Notice.AAA6.DefResponse.Full DefenceOrPartAdmin.AlreadyPaid.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant" : ["applicant1PartyName", "admissionPaidAmount"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant',
        'Wait for the court to review the case',
        'Wait for the court to review the case',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed to court.</p><p class="govuk-body">They rejected your admission of ${admissionPaidAmount} although they accept you have already paid it.</p><p class="govuk-body">If the case goes to a hearing we will contact you with further details.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View your response</a><br><a target="_blank" href="{VIEW_CLAIMANT_HEARING_REQS}"  rel="noopener noreferrer" class="govuk-link">View the claimant''s hearing requirements</a></p>',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed to court.</p><p class="govuk-body">They rejected your admission of ${admissionPaidAmount} although they accept you have already paid it.</p><p class="govuk-body">If the case goes to a hearing we will contact you with further details.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View your response</a><br><a target="_blank" href="{VIEW_CLAIMANT_HEARING_REQS}"  rel="noopener noreferrer" class="govuk-link">View the claimant''s hearing requirements</a></p>',
        'DEFENDANT');
