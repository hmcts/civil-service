/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.ClaimantEndsClaim.Claimant', '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant","Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant","Notice.AAA6.DefResponse.FullDefence.FullDispute.FastTrack.Claimant","Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.ClaimantEndsClaim.Claimant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.ClaimantEndsClaim.Claimant', 'The claim has now ended' , 'Mae’r hawliad wedi dod i ben',
        '<p class="govuk-body">You have decided not to proceed with the claim.</p>',
        '<p class="govuk-body">Rydych chi wedi penderfynu peidio â bwrw ymlaen gyda’r hawliad.</p>',
        'CLAIMANT');
