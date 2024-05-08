/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullDefence.FullDispute.FastTrack.Claimant',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment","Notice.AAA6.DefResponse.MoretimeRequested.Claimant","Notice.AAA6.ClaimIssue.HWF.FullRemission"}',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.FastTrack.Claimant" : ["respondent1PartyName", "applicant1ResponseDeadlineEn", "applicant1ResponseDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.FullDefence.FullDispute.FastTrack.Claimant', 'Response to the claim', 'Ymateb i’r hawliad',
        '<p class="govuk-body">${respondent1PartyName} has rejected the claim.<br>You need to respond by ${applicant1ResponseDeadlineEn}.<br><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">View and respond</a>.</p>',
        '<p class="govuk-body">Mae ${respondent1PartyName} wedi gwrthod yr hawliad.<br>Mae angen i chi ymateb erbyn ${applicant1ResponseDeadlineCy}.<br><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">Gweld ac ymateb</a>.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM}  rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>','The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM}  rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>','The response',
        'Response.View', 'Scenario.AAA6.DefResponse.FullDefence.FullDispute.FastTrack.Claimant', '{3, 3}', 'CLAIMANT', 3);
