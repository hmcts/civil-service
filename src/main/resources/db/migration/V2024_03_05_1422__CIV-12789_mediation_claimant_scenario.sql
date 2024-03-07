/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.Mediation.Claimant', '{"Notice.AAA7.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA7.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant","Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant","Notice.AAA7.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant","Notice.AAA7.DefResponse.PartAdmit.AlreadyPaid.Claimant","Notice.AAA7.DefResponse.FullDefence.AlreadyPaid.Claimant","Notice.AAA7.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant","Notice.AAA7.DefResponse.PartAdmit.PayImmediately.Claimant"}',
        '{"Notice.AAA7.ClaimantIntent.Mediation.Claimant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.Mediation.Claimant', 'You’ve rejected the defendant’s response' , 'You’ve rejected the defendant’s response',
        'You’ve both agreed to try mediation. Your mediation appointment will be arranged within 28 days. <a href="https://www.gov.uk/guidance/small-claims-mediation-service"  rel="noopener noreferrer" class="govuk-link"> Find out more about how mediation works (opens in new tab)</a>.',
        'You’ve both agreed to try mediation. Your mediation appointment will be arranged within 28 days. <a href="https://www.gov.uk/guidance/small-claims-mediation-service"  rel="noopener noreferrer" class="govuk-link"> Find out more about how mediation works (opens in new tab)</a>.',
        'CLAIMANT');
