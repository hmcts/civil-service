/**
 * update format on the notification links removing new lines
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = replace(description_En, '</p><p class="govuk-body">', ' '), description_Cy = replace(description_Cy, '</p><p class="govuk-body">', ' ')
WHERE template_name in ('Notice.AAA6.CP.Hearing.Scheduled.Claimant', 'Notice.AAA6.CP.Hearing.Scheduled.Defendant', 'Notice.AAA6.CP.OrderMade.Claimant', 'Notice.AAA6.CP.OrderMade.Defendant', 'Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant', 'Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Defendant', 'Notice.AAA6.ClaimIssue.Response.Required');
