/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant', '{"Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant"}', '{"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant" : ["applicant1PartyName", "defendantAdmittedAmount", "instalmentAmount", "paymentFrequency", "paymentFrequencyWelsh", "instalmentStartDateEn", "instalmentStartDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant', 'Response to the claim', 'Ymateb i’r hawliad',
        '<p class="govuk-body">You have offered to pay ${applicant1PartyName} ${defendantAdmittedAmount} in instalments of ${instalmentAmount} ${paymentFrequency}. You have offered to do this starting from ${instalmentStartDateEn}. You need to send the claimant your financial details. The court will contact you when they respond.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">View your response</a></p>',
        '<p class="govuk-body">Rydych wedi cynnig talu ${defendantAdmittedAmount} mewn rhandaliadau o ${instalmentAmount} i ${applicant1PartyName} ${paymentFrequencyWelsh}. Rydych wedi cynnig gwneud hyn o ${instalmentStartDateCy} ymlaen. Mae angen i chi anfon eich manylion ariannol at yr hawlydd. Bydd y llys yn cysylltu â chi pan fyddant yn ymateb.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}" class="govuk-link">Gweld eich ymateb</a></p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>',
        'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant', '{3, 3}', 'DEFENDANT', 3);
