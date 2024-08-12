/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimantEndsClaim.Defendant', '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant","Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Defendant","Notice.AAA6.DefResponse.FullDefence.FullDispute.FastTrack.Defendant","Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.ClaimantEndsClaim.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimantEndsClaim.Defendant', 'The claim has now ended' , 'Mae’r hawliad wedi dod i ben',
        '<p class="govuk-body">${applicant1PartyName} has decided not to proceed with the claim.</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} wedi penderfynu peidio â bwrw ymlaen gyda’r hawliad.</p>',
        'DEFENDANT');
