/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoretimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.FullRemission"}',
        '{"Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant" : ["claimSettledAmount", "claimSettledDateEn", "claimSettledDateCy", "defaultRespondTime", "applicant1ResponseDeadlineEn", "applicant1ResponseDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant', 'Response to the claim', 'Response to the claim',
        '<p class="govuk-body">The defendant has said they already paid ${claimSettledAmount} on ${claimSettledDateEn}.</p><p class="govuk-body">You can confirm payment and settle, or proceed with the claim.</p><p class="govuk-body">You need to respond by ${defaultRespondTime} on ${applicant1ResponseDeadlineEn} or the claim will not continue.</p><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">View and respond</a>',
        '<p class="govuk-body">The defendant has said they already paid ${claimSettledAmount} on ${claimSettledDateCy}.</p><p class="govuk-body">You can confirm payment and settle, or proceed with the claim.</p><p class="govuk-body">You need to respond by ${defaultRespondTime} on ${applicant1ResponseDeadlineCy} or the claim will not continue.</p><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">View and respond</a>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant', '{3, 3}', 'CLAIMANT', 3);
