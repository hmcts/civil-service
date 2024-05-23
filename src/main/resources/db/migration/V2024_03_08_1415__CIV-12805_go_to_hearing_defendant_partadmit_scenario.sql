/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant",
        "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant",
        "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant"}', '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant":["applicant1PartyName","defendantAdmittedAmount"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant', 'Wait for the court to review the case', 'Wait for the court to review the case',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed with the claim.</p><p class="govuk-body">They rejected your admission of ${defendantAdmittedAmount}.</p><p class="govuk-body">The case will be referred to a judge who will decide what should happen next.</p><p class="govuk-body">You can <a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">view your response</a>'
          ' or <a href={VIEW_CLAIMANT_HEARING_REQS} target="_blank" class="govuk-link">view the claimant''s hearing requirements</a>.</p>',
        '<p class="govuk-body">${applicant1PartyName} wants to proceed with the claim.</p><p class="govuk-body">They rejected your admission of ${defendantAdmittedAmount}.</p><p class="govuk-body">The case will be referred to a judge who will decide what should happen next.</p><p class="govuk-body">You can <a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">view your response</a>'
          ' or <a href={VIEW_CLAIMANT_HEARING_REQS} target="_blank" class="govuk-link">view the claimant''s hearing requirements</a>.</p>',
        'DEFENDANT');
