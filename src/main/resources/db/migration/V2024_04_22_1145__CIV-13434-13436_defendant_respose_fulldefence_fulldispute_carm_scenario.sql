/**
 * Add scenario for defendant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullDefence.FullDispute.CARM.Defendant',
        '{"Notice.AAA6.ClaimIssue.Response.Required",
        "Notice.AAA6.DefResponse.MoretimeRequested.Defendant",
        "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant",
        "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Defendant",
        "Notice.AAA6.DefResponse.Full Defence.FullDispute.SuggestedMediation.Defendant",
        "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Defendant"}',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Defendant":["applicant1PartyName"]}');

/**
 * Add notification template for defendant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Defendant',
        'Response to the claim',
        'Ymateb i’r hawliad',
        '<p class="govuk-body">You have rejected the claim. The court will contact you when ${applicant1PartyName} responds.</p><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a>',
        '<p class="govuk-body">Rydych wedi gwrthod yr hawliad. Bydd y llys yn cysylltu â chi pan fydd ${applicant1PartyName} yn ymateb.</p><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">Gweld eich ymateb</a>',
        'DEFENDANT');

/**
 * Add task list items defendant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.CARM.Defendant', '{3, 3}', 'DEFENDANT', 3);

/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
        "Notice.AAA6.ClaimIssue.Response.Await",
        "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoretimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.FullRemission",
        "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant",
        "Notice.AAA6.DefResponse.Full Defence.FullDispute.SuggestedMediation.Claimant",
        "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant"}',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant":["respondent1PartyName", "applicant1ResponseDeadlineEn", "applicant1ResponseDeadlineCy"]}');

/**
 * Add notification template for claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant',
        'Response to the claim',
        'Ymateb i’r hawliad',
        '<p class="govuk-body">${respondent1PartyName} has rejected the claim. You need to respond by ${applicant1ResponseDeadlineEn}.</p><a href="{VIEW_AND_RESPOND}" class="govuk-link">View and respond</a>',
        '<p class="govuk-body">Mae ${respondent1PartyName} wedi gwrthod yr hawliad. Mae angen i chi ymateb erbyn ${applicant1ResponseDeadlineCy}.</p><a href="{VIEW_AND_RESPOND}" class="govuk-link">Gweld ac ymateb</a>',
        'CLAIMANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant', '{3, 3}', 'CLAIMANT', 3);
