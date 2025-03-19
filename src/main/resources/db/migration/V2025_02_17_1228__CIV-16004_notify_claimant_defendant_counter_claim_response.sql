/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.Notice.AAA6.DefLRResponse.FullDefence.Counterclaim.Claimant',
        '{"Notice.AAA6.ClaimIssue.Response.Await","Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant","Notice.AAA6.ClaimIssue.HWF.PhonePayment","Notice.AAA6.DefResponse.MoretimeRequested.Claimant","Notice.AAA6.ClaimIssue.HWF.FullRemission"}',
        '{"Notice.AAA6.DefLRResponse.FullDefence.Counterclaim.Claimant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.DefLRResponse.FullDefence.Counterclaim.Claimant',
        'The defendant’s legal representative wishes to defend the claim and counter claim',
        '|Mae cynrychiolydd cyfreithiol y diffynnydd yn dymuno amddiffyn yr hawliad a’r gwrth-hawliad',
        '<p class="govuk-body">The defendant must file the response by the deadline. If they do not respond by their deadline then you can request a county court judgment by using form <a href="https://www.gov.uk/government/publications/form-n225-request-for-judgment-and-reply-to-admission-specified-amount" target="_blank" class="govuk-link">N225</a>. This claim will now proceed offline.</p>',
        '<p class="govuk-body">Rhaid i''r diffynnydd gyflwyno''r ymateb erbyn y dyddiad cau. Os na fyddant yn ymateb erbyn y dyddiad cau, gallwch ofyn am ddyfarniad llys sirol drwy ddefnyddio ffurflen <a href="https://www.gov.uk/government/publications/form-n225-request-for-judgment-and-reply-to-admission-specified-amount" target="_blank" class="govuk-link">N225</a>. Bydd yr hawliad hwn yn mynd rhagddo all-lein yn awr.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.Notice.AAA6.DefLRResponse.FullDefence.Counterclaim.Claimant', '{3, 3}',
        'CLAIMANT', 3);
