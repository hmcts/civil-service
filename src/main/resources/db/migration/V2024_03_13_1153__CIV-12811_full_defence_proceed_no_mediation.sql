/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Defendant", "Notice.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant',
        'Wait for the court to review the case', 'Aros i’r llys adolygu’r achos',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed with the claim.'
          ' The case will be referred to a judge who will decide what should happen next.</p>'
          '<p class="govuk-body">You can <a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">view your response</a> or '
          '<a href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link" target="_blank">view the claimant''s hearing requirements (opens in a new tab)</a>.</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} eisiau parhau â’r hawliad.'
          ' Bydd yr achos yn cael ei gyfeirio at farnwr a fydd yn penderfynu beth ddylai ddigwydd nesaf.</p>'
          '<p class="govuk-body">Gallwch <a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">weld eich ymateb</a> neu '
          '<a href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link" target="_blank">weld y gofynion ar gyfer gwrandawiad yr hawlydd (yn agor mewn tab newydd)</a>.</p>',
        'DEFENDANT');
