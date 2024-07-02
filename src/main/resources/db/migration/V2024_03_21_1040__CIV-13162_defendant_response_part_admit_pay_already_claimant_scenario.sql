/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.DefResponse.BilingualFlagSet.Claimant"}',
        '{"Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant" : ["admissionPaidAmount", "claimSettledDateEn", "claimSettledDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant', 'Response to the claim', 'Ymateb i’r hawliad',
        '<p class="govuk-body">The defendant has said they already paid ${admissionPaidAmount} on ${claimSettledDateEn}. You can confirm payment and settle, or proceed with the claim.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">View and respond</a></p>',
        '<p class="govuk-body">Mae’r diffynnydd wedi dweud eu bod wedi talu ${admissionPaidAmount} yn barod ar ${claimSettledDateCy}. Gallwch gadarnhau bod y taliad wedi’i wneud a setlo, neu barhau â’r hawliad.</p><p class="govuk-body"><a href="{CLAIMANT_RESPONSE_TASK_LIST}" class="govuk-link">Gweld ac ymateb</a></p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>', 'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant', '{3, 3}', 'CLAIMANT', 3);
