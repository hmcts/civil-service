/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Defendant',
        '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant" :  ["defendantAdmittedAmount", "installmentAmount", "paymentFrequency","paymentFrequencyWelsh","firstRepaymentDateEn","firstRepaymentDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant', 'Response to the claim', 'Ymateb i’r hawliad',
        '<p class="govuk-body">You have offered to pay ${defendantAdmittedAmount} in instalments of ${installmentAmount} ${paymentFrequency}. You have offered to do this starting from ${firstRepaymentDateEn}. We will contact you when the claimant responds to your offer.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a></p>',
        '<p class="govuk-body">Rydych wedi cynnig talu ${defendantAdmittedAmount} mewn rhandaliadau o ${installmentAmount} ${paymentFrequencyWelsh}. Rydych wedi cynnig gwneud hyn o ${firstRepaymentDateCy} ymlaen. Byddwn yn cysylltu â chi pan fydd yr hawlydd yn ymateb i’ch cynnig.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">Gweld eich ymateb</a></p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Defendant', '{3, 3}', 'DEFENDANT', 3);
