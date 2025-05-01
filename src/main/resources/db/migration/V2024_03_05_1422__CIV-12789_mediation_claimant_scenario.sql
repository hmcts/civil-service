/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.Mediation.Claimant', '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant","Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant","Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant","Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant","Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant","Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant","Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.Mediation.Claimant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.Mediation.Claimant', 'You have said you wish continue with your claim',
        'Rydych wedi dweud eich bod yn dymuno parhau â’ch hawliad',
        '<p class="govuk-body">Your case will be referred for mediation. Your mediation appointment will be arranged within 28 days.</p><p class="govuk-body"><a href="https://www.gov.uk/guidance/small-claims-mediation-service"  rel="noopener noreferrer" class="govuk-link" target="_blank"> Find out more about how mediation works (opens in new tab)</a>.</p>',
        '<p class="govuk-body">Bydd eich achos yn cael ei gyfeirio at y gwasanaeth cyfryngu. Bydd eich apwyntiad cyfryngu yn cael ei drefnu o fewn 28 diwrnod.</p><p class="govuk-body"><a href="https://www.gov.uk/guidance/small-claims-mediation-service"  rel="noopener noreferrer" class="govuk-link" target="_blank"> Rhagor o wybodaeth am sut mae cyfryngu yn gweithio (yn agor mewn tab newydd)</a>.</p>',
        'CLAIMANT');
