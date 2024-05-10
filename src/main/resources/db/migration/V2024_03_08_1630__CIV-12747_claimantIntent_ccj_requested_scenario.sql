/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.CCJ.Requested.Defendant', '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant',
        '${applicant1PartyName} has requested a County Court Judgment (CCJ)',
        '${applicant1PartyName} has requested a County Court Judgment (CCJ)',
        '<p class="govuk-body">${applicant1PartyName} has requested a CCJ against you because you have not responded to the claim and the response deadline has passed.</p>'
          '<p class="govuk-body">Your online account will not be updated with the progress of the claim, and any further updates will be by post.</p>'
          '<p class="govuk-body">If your deadline has passed, but the CCJ has not been issued, you can still respond. Get in touch with HM Courts and Tribunals Service (HMCTS) on {civilMoneyClaimsTelephone} if you are in England and Wales, or 0300 790 6234 if you are in Scotland. ' ||
        'You can call from Monday to Friday, between 8.30am to 5pm. <a href="https://www.gov.uk/call-charges" target="_blank" rel="noopener noreferrer" class="govuk-link">Find out about call charges (opens in new tab).</a></p>'
          '<p class="govuk-body">If you do not get in touch, we will post a CCJ to yourself and ${applicant1PartyName} and explain what to do next.</p>',
        '<p class="govuk-body">${applicant1PartyName} has requested a CCJ against you because you have not responded to the claim and the response deadline has passed.</p>'
          '<p class="govuk-body">Your online account will not be updated with the progress of the claim, and any further updates will be by post.</p>'
          '<p class="govuk-body">If your deadline has passed, but the CCJ has not been issued, you can still respond. Get in touch with HM Courts and Tribunals Service (HMCTS) on {civilMoneyClaimsTelephone} if you are in England and Wales, or 0300 790 6234 if you are in Scotland. ' ||
        'You can call from Monday to Friday, between 8.30am to 5pm. <a href="https://www.gov.uk/call-charges" target="_blank" rel="noopener noreferrer" class="govuk-link">Find out about call charges (opens in new tab).</a></p>'
          '<p class="govuk-body">If you do not get in touch, we will post a CCJ to yourself and ${applicant1PartyName} and explain what to do next.</p>',
        'DEFENDANT');
