/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant',
        '{"Notice.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant" : ["applicant1PartyName","claimSettledAmount"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant',
        'Wait for the court to review the case',
        'Aros i’r llys adolygu’r achos',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed with the claim.'
          ' They said you have not paid the ${claimSettledAmount} you admit you owe.'
          ' The case will be referred to a judge who will decide what should happen next.</p>'
          '<p class="govuk-body">You can <a href="{VIEW_RESPONSE_TO_CLAIM}" rel="noopener noreferrer" class="govuk-link">view your response</a> or <a target="_blank" href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link">view the claimant''s hearing requirements (opens in a new tab)</a>.</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} eisiau parhau â’r hawliad.'
          ' Maent yn dweud nad ydych wedi talu ${claimSettledAmount} sef y swm rydych yn cyfaddef sy’n ddyledus gennych.'
          ' Bydd yr achos yn cael ei gyfeirio at farnwr a fydd yn penderfynu beth ddylai ddigwydd nesaf.</p>'
          '<p class="govuk-body">Gallwch <a href="{VIEW_RESPONSE_TO_CLAIM}" rel="noopener noreferrer" class="govuk-link">weld eich ymateb</a> neu <a target="_blank" href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link">weld y gofynion ar gyfer gwrandawiad yr hawlydd (yn agor mewn tab newydd)</a>.</p>',
        'DEFENDANT');
