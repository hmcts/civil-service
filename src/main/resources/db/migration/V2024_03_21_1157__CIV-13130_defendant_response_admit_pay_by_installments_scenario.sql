INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant',
        '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.DefResponse.MoretimeRequested.Claimant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant"}', '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant":["defendantAdmittedAmount", "installmentAmount", "installmentTimePeriod", "installmentStartDateEn", "installmentStartDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant', 'Response to the claim', 'The claim is settled',
        '<p class="govuk-body">The defendant has offered to pay ${defendantAdmittedAmount} in installments of ${installmentAmount} {installmentTimePeriod} starting ${installmentStartDateEn}.</p><p class="govuk-body"><a href="{VIEW_AND_RESPOND}"  rel="noopener noreferrer" class="govuk-link">View and respond</a></p>',
        '<p class="govuk-body">The defendant has offered to pay ${defendantAdmittedAmount} in installments of ${installmentAmount} {installmentTimePeriod} starting ${installmentStartDateCy}.</p><p class="govuk-body"><a href="{VIEW_AND_RESPOND}"  rel="noopener noreferrer" class="govuk-link">View and respond</a></p>',
        'CLAIMANT');
