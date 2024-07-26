/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant":["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant', 'Mediation was unsuccessful', 'Nid oedd y cyfryngu yn llwyddiannus',
        '<p class="govuk-body">You weren''t able to resolve ${applicant1PartyName}''s claim against you using mediation. The court will review the case. We''ll contact you to tell you what to do next. <a href={VIEW_CLAIMANT_HEARING_REQS} target="_blank" class="govuk-link">View ${applicant1PartyName}''s hearing requirements (opens in a new tab)</a>.</p>',
        '<p class="govuk-body">Nid oeddech yn gallu datrys hawliad ${applicant1PartyName} yn eich erbyn drwy gyfryngu. Bydd y llys yn adolygu’r achos. Mi wnawn gysylltu â chi i ddweud wrthych beth i’w wneud nesaf. <a href={VIEW_CLAIMANT_HEARING_REQS} target="_blank" class="govuk-link">Gweld gofynion ar gyfer y gwrandawiad ${applicant1PartyName} (yn agor mewn tab newydd)</a>.</p>',
        'DEFENDANT');
