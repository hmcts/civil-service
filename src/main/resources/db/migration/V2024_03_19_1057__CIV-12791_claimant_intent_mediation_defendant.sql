/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.Mediation.Defendant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant", '
        '"Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant", '
        '"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant", '
        '"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant", '
        '"Notice.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant", '
        '"Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Defendant", '
        '"Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.Mediation.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.Mediation.Defendant',
        '${applicant1PartyName} rejected your response', 'Mae ${applicant1PartyName} wedi gwrthod eich ymateb',
        '<p class="govuk-body">Your case will be referred for mediation. Your mediation appointment will be arranged within 28 days.</p>'
          '<p class="govuk-body"><a href="{MEDIATION}" rel="noopener noreferrer" class="govuk-link" target="_blank">Find out more about how mediation works (opens in a new tab)</a>.<p/>'
          '<p class="govuk-body">They''ve also sent us their hearing requirements. '
          '<a href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link" target="_blank">You can view their hearing requirements here (opens in new tab)</a>.</p>',
        '<p class="govuk-body">Bydd eich achos yn cael ei gyfeirio at y gwasanaeth cyfryngu. Bydd eich apwyntiad cyfryngu yn cael ei drefnu o fewn 28 diwrnod.</p>'
          '<p class="govuk-body"><a href="{MEDIATION}" rel="noopener noreferrer" class="govuk-link" target="_blank">Rhagor o wybodaeth am sut mae cyfryngu yn gweithio (yn agor mewn tab newydd)</a>.<p/>'
          '<p class="govuk-body">Maent hefyd wedi anfon atom eu gofynion ar gyfer y gwrandawiad. '
          '<a href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link" target="_blank">Gallwch weld eu gofynion ar gyfer y gwrandawiad yma (yn agor mewn tab newydd)</a>.</p>',
        'DEFENDANT');
