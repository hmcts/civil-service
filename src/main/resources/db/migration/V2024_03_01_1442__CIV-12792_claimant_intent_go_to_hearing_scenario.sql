/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.GoToHearing.Claimant',
        '{"Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant", "Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant"}', '{"Notice.AAA6.ClaimantIntent.GoToHearing.Claimant":["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.GoToHearing.Claimant', 'Wait for the court to review the case', 'Aros i’r llys adolygu’r achos',
        '<p class="govuk-body">You have rejected ${respondent1PartyName}''s response. The case will be referred to a judge who will decide what should happen next.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">View the defendant''s response</a></p>',
        '<p class="govuk-body">Rydych wedi gwrthod ymateb ${respondent1PartyName}. Bydd yr achos yn cael ei gyfeirio at farnwr a fydd yn penderfynu beth ddylai ddigwydd nesaf.</p><p class="govuk-body"><a href="{VIEW_RESPONSE_TO_CLAIM}"  rel="noopener noreferrer" class="govuk-link">Gweld ymateb y diffynnydd</a></p>',
        'CLAIMANT');
