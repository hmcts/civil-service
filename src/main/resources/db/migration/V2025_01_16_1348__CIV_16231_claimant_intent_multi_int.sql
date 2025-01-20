/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.Multi.Int.Await.Claimant',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.Multi.Int.Await.Claimant": ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy,
                                                   notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.Multi.Int.Await.Claimant',
        'Wait for the court to review the case',
        'Aros i''r llys adolygu''r achos',
        '<p class="govuk-body">You have responded to ${respondent1PartyName}, the court will now review the case, You will be contacted if a hearing is needed in this case.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View the Defendant''s response</a></p>',
        '<p class="govuk-body">Rydych wedi ymateb i ${respondent1PartyName}, bydd y llys nawr yn adolygu''r achos. Cysylltir â chi os oes angen gwrandawiad yn yr achos hwn.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">Gweld ymateb y Diffynnydd</a></p>',
        'CLAIMANT');


INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.Multi.Int.Await.Defendant',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.Multi.Int.Await.Defendant": ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy,
                                                   notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.Multi.Int.Await.Defendant',
        'Wait for the court to review the case',
        'Aros i''r llys adolygu''r achos',
        '<p class="govuk-body">${applicant1PartyName} has responded, the court will now review the case. You will be contacted if a hearing is needed in this case.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" rel="noopener noreferrer" class="govuk-link">View your response</a> <br> <a target="_blank" href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link">View the claimant''s hearing requirements</a></p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} wedi ymateb, bydd y llys nawr yn adolygu''r achos. Cysylltir â chi os oes angen gwrandawiad yn yr achos hwn.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" rel="noopener noreferrer" class="govuk-link">Gweld eich ymateb</a> <br> <a target="_blank" href="{VIEW_CLAIMANT_HEARING_REQS}" rel="noopener noreferrer" class="govuk-link">Gweld gofynion gwrandawiad yr hawlydd</a></p>',
        'DEFENDANT');
