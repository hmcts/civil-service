/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.FastTrack.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant',
        'Wait for the court to review the case', 'Wait for the court to review the case',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed to court.</p>'
        '<p class="govuk-body">They rejected your response.</p>'
        '<p class="govuk-body">If the case goes to a hearing we will contact you with further details.</p>'
        '<p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a><br>'
        '<a href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link" target="_blank">View the claimant''s hearing requirements</a></p>',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed to court.</p>'
        '<p class="govuk-body">They rejected your response.</p>'
        '<p class="govuk-body">If the case goes to a hearing we will contact you with further details.</p>'
        '<p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a><br>'
        '<a href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link" target="_blank">View the claimant''s hearing requirements</a></p>',
        'DEFENDANT');
