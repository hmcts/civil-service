/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant',
        '{"Notice.AAA7.DefResponse.FullDefence.FullDispute.SuggestedMediation.Defendant"}',
        '{"Notice.AAA7.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant":["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant',
        'Wait for the court to review the case', 'Wait for the court to review the case',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed to court.</p> <p class="govuk-body">They rejected your response.</p> <p class="govuk-body">They said no to mediation.</p> <p class="govuk-body">If the case goes to a hearing we will contact you with further details.</p> <p class="govuk-body"><a href={VIEW_DEFENDANT_RESPONSE} class="govuk-link">View your response</a><br><a href={VIEW_CLAIMANT_HEARING_REQS} rel="noopener noreferrer" class="govuk-link">View the claimant''s hearing requirements</a></p>',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed to court.</p> <p class="govuk-body">They rejected your response.</p> <p class="govuk-body">They said no to mediation.</p> <p class="govuk-body">If the case goes to a hearing we will contact you with further details.</p> <p class="govuk-body"><a href={VIEW_DEFENDANT_RESPONSE} class="govuk-link">View your response</a><br><a href={VIEW_CLAIMANT_HEARING_REQS} rel="noopener noreferrer" class="govuk-link">View the claimant''s hearing requirements</a></p>',
        'DEFENDANT');

/**
 * Add task list items
 * No required
 */
