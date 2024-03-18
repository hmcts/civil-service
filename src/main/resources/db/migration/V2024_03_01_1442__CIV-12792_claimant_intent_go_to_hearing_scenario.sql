/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.GoToHearing.Claimant',
        '{"Notice.AAA7.DefResponse.PartAdmit.PayImmediately.Claimant", "Notice.AAA7.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA7.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant", "Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant", "Notice.AAA7.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA7.DefResponse.FullDefence.AlreadyPaid.Claimant", "Notice.AAA7.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant", "Notice.AAA7.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant"}', '{"Notice.AAA7.ClaimantIntent.GoToHearing.Claimant":["defendantName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.GoToHearing.Claimant', 'Wait for the court to review the case', 'Wait for the court to review the case',
        '<p class="govuk-body">You have rejected ${defendantName}''s response and want to proceed to court. If the case goes to a hearing we will contact you with further details.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View the defendant''s response</a>.</p>',
        '<p class="govuk-body">You have rejected ${defendantName}''s response and want to proceed to court. If the case goes to a hearing we will contact you with further details.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View the defendant''s response</a>.</p>',
        'CLAIMANT');
