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
VALUES ('Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant', 'Mediation was unsuccessful', 'Mediation was unsuccessful',
        '<p class="govuk-body">You weren''t able to resolve ${applicant1PartyName}''s claim against you using mediation. The court will review the case. We''ll contact you to tell you what to do next. <a href={VIEW_CLAIMANT_HEARING_REQS} target="_blank" class="govuk-link">View ${applicant1PartyName}''s hearing requirements.</a></p>',
        '<p class="govuk-body">You weren''t able to resolve ${applicant1PartyName}''s claim against you using mediation. The court will review the case. We''ll contact you to tell you what to do next. <a href={VIEW_CLAIMANT_HEARING_REQS} target="_blank" class="govuk-link">View ${applicant1PartyName}''s hearing requirements.</a></p>',
        'DEFENDANT');
