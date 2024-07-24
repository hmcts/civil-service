/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.CCJ.Requested.Defendant', '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant',
        '${applicant1PartyName} has requested a County Court Judgment (CCJ)',
        'Mae ${applicant1PartyName} wedi gwneud cais am Ddyfarniad Llys Sirol (CCJ)',
        '<p class="govuk-body">${applicant1PartyName} has requested a CCJ against you, because you have not responded to the claim and the response deadline has passed.</p>'
        '<p class="govuk-body">We will post a CCJ to you and ${applicant1PartyName} and explain what to do next.</p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} wedi gwneud cais am CCJ yn eich erbyn oherwydd nid ydych wedi ymateb i’r hawliad ac mae’r terfyn amser ar gyfer ymateb wedi bod.</p>'
        '<p class="govuk-body">Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru gyda manylion cynnydd yr hawliad, a bydd unrhyw ddiweddariadau pellach yn cael eu hanfon drwy’r post.</p>'
        '<p class="govuk-body">Os yw eich terfyn amser wedi pasio, ond nad yw’r CCJ wedi’i gyhoeddi, gallwch dal ymateb. Cysylltwch â Gwasanaeth Llysoedd a Thribiwnlysoedd EF (GLlTEF) ar {civilMoneyClaimsTelephone} os ydych yn Nghymru a Lloegr. ' ||
        'Gallwch ffonio rhwng 8.30am a 5pm dydd Llun i ddydd Gwener. <a href="https://www.gov.uk/call-charges" target="_blank" rel="noopener noreferrer" class="govuk-link">Gwybodaeth am gost galwadau (yn agor mewn tab newydd)</a>.</p>'
          '<p class="govuk-body">Os na fyddwch yn cysylltu, byddwn yn anfon CCJ drwy’r post atoch chi a ${applicant1PartyName} ac yn egluro beth i’w wneud nesaf.</p>',
        'DEFENDANT');
