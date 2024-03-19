/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant',
        '{"Notice.AAA7.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA7.ClaimIssue.Response.Await","Notice.AAA7.ClaimIssue.HWF.PhonePayment","Notice.AAA7.DefResponse.MoretimeRequested.Claimant","Notice.AAA7.ClaimIssue.HWF.FullRemission"}',
        '{"Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant": ["defendantAdmittedAmount", "installmentAmount", "paymentFrequency","firstRepaymentDate"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant',
        'Response to the claim', 'Response to the claim',
        '<p class="govuk-body">The defendant has offered to pay ${defendantAdmittedAmount} in instalments of ${installmentAmount} every ${paymentFrequency}.They are offering to do this starting from ${firstRepaymentDate}.</p><p class="govuk-body">The defendant needs to send you their financial details. <a href="{VIEW_AND_RESPOND}" rel="noopener noreferrer" class="govuk-link">View and respond</a></p>',
        '',
        'CLAIMANT');

