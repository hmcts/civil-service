/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await","Notice.AAA6.ClaimIssue.HWF.PhonePayment","Notice.AAA6.DefResponse.MoretimeRequested.Claimant","Notice.AAA6.ClaimIssue.HWF.FullRemission"}',
        '{"Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant": ["defendantAdmittedAmount", "installmentAmount", "paymentFrequency","firstRepaymentDateEn","firstRepaymentDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant',
        'Response to the claim', 'Response to the claim',
        '<p class="govuk-body">The defendant has offered to pay ${defendantAdmittedAmount} in instalments of ${installmentAmount} ${paymentFrequency}.They are offering to do this starting from ${firstRepaymentDateEn}.</p><p class="govuk-body">The defendant needs to send you their financial details.</p> <a href="{VIEW_AND_RESPOND}" rel="noopener noreferrer" class="govuk-link">View and respond</a>',
        '<p class="govuk-body">The defendant has offered to pay ${defendantAdmittedAmount} in instalments of ${installmentAmount} ${paymentFrequency}.They are offering to do this starting from ${firstRepaymentDateCy}.</p><p class="govuk-body">The defendant needs to send you their financial details.</p> <a href="{VIEW_AND_RESPOND}" rel="noopener noreferrer" class="govuk-link">View and respond</a>',
        'CLAIMANT');

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>', 'The response','<a href={VIEW_RESPONSE_TO_CLAIM}>View the response to the claim</a>',
        'The response', 'Response.View', 'Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant', '{3, 3}', 'CLAIMANT', 3);


