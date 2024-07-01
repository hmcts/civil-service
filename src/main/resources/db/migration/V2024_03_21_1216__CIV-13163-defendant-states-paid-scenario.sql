/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant', '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant", "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}', '{"Notice.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant" : ["admissionPaidAmount", "claimSettledDateEn", "claimSettledDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant', 'Response to the claim', 'Ymateb i’r hawliad',
        '<p class="govuk-body">You have said you already paid ${admissionPaidAmount} on ${claimSettledDateEn}. The claimant can confirm payment and settle, or proceed with the claim. The court will contact you when they respond.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a></p>',
        '<p class="govuk-body">Rydych wedi dweud eich bod wedi talu ${admissionPaidAmount} yn barod ar ${claimSettledDateCy}. Gall yr hawlydd gadarnhau bod y taliad wedi’i wneud a setlo, neu barhau â’r hawliad. Bydd y llys yn cysylltu â chi pan fyddant yn ymateb.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">Gweld eich ymateb</a></p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant', '{3, 3}', 'DEFENDANT', 3);
