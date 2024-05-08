/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant":["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant',
        'Wait for the court to review the case', 'Aros i’r llys adolygu’r achos',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed with the claim.</p> <p class="govuk-body">They said no to mediation.</p> <p class="govuk-body">The case will be referred to a judge who will decide what should happen next.</p> <p class="govuk-body">You can <a href={VIEW_DEFENDANT_RESPONSE} class="govuk-link">view your response</a> or <a target="_blank" href={VIEW_CLAIMANT_HEARING_REQS} rel="noopener noreferrer" class="govuk-link">view the claimant''s hearing requirements</a>.</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} eisiau parhau â’r hawliad.</p> <p class="govuk-body">Mi wnaethant wrthod rhoi cynnig ar gyfryngu.</p> <p class="govuk-body">Bydd yr achos yn cael ei gyfeirio at farnwr a fydd yn penderfynu beth ddylai ddigwydd nesaf.</p> <p class="govuk-body">Gallwch <a href={VIEW_DEFENDANT_RESPONSE} class="govuk-link">weld eich ymateb</a> neu <a target="_blank" href={VIEW_CLAIMANT_HEARING_REQS} rel="noopener noreferrer" class="govuk-link">weld gofynion ar gyfer y gwrandawiad yr hawlydd</a>.</p>',
        'DEFENDANT');

/**
 * Add task list items
 * No required
 */
