/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.Mediation.Claimant', '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant","Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant","Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant","Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant","Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant","Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant","Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.Mediation.Claimant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.Mediation.Claimant', 'You’ve rejected the defendant’s response' , 'You’ve rejected the defendant’s response',
        '<p class="govuk-body">You’ve both agreed to try mediation. Your mediation appointment will be arranged within 28 days.</p><p class="govuk-body"><a href="https://www.gov.uk/guidance/small-claims-mediation-service"  rel="noopener noreferrer" class="govuk-link"> Find out more about how mediation works (opens in new tab)</a>.</p>',
        '<p class="govuk-body">You’ve both agreed to try mediation. Your mediation appointment will be arranged within 28 days.</p><p class="govuk-body"><a href="https://www.gov.uk/guidance/small-claims-mediation-service"  rel="noopener noreferrer" class="govuk-link"> Find out more about how mediation works (opens in new tab)</a>.</p>',
        'CLAIMANT');
