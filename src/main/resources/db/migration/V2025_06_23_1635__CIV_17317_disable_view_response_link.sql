INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Disable.View.Response.Link', '{"Notice.AAA6.ClaimIssue.Response.Await",
          "Notice.AAA6.DefResponse.MoretimeRequested.Claimant",
          "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
          "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}',
        '{"Notice.AAA6.DefResponse.BilingualFlagSet.Claimant": []}');

/**
 * Add notification template for claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.DefResponse.BilingualFlagSet.Claimant',
        'The defendant''s response is being translated',
        'Mae ymateb y diffynnydd yn cael ei gyfieithu',
        '<p class="govuk-body">The defendant has chosen to respond to the claim in Welsh. Their response is paused for translation into English. We will send it to you when it has been translated.</p>',
        '<p class="govuk-body">Mae''r diffynnydd wedi dewis ymateb i''r cais yn Gymraeg. Mae ei ymateb yn cael ei gyfieithu i''r Saesneg. Byddwn yn ei anfon atoch pan fydd wedi’i gyfieithu.</p>',
        'CLAIMANT');


INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a>View the response to the claim</a>',
        'The response',
        '<a>View the response to the claim</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.Disable.View.Response.Link', '{1, 1}', 'CLAIMANT', 3);

-- ('<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>',
--   'The response',
--   '<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">Gweld gwybodaeth am y diffynnydd</a>',
--   'Yr ymateb', 'Response.Defendant.Info', 'Scenario.AAA6.Disable.View.Response.Link', '{3, 3}', 'CLAIMANT', 4);
