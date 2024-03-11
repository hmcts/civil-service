/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimantIntent.CCJ.Requested.Defendant', '{"Notice.AAA7.DefResponse.ResponseTimeElapsed.Defendant", "Notice.AAA7.DefResponse.FullOrPartAdmit.PayImmediately.Defendant"}',
        '{"Notice.AAA7.ClaimantIntent.CCJ.Requested.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimantIntent.CCJ.Requested.Defendant', 'County Court Judgment (CCJ) requested', 'County Court Judgment (CCJ) requested',
        '<p class="govuk-body">{applicant1PartyName} has requested CCJ against you, because the response deadline has passed.</p>'
        '<p class="govuk-body">Your online account will not be updated with the progress of the claim, and any further updates will be by post.</p>'
        '<p class="govuk-body">If your deadline has passed, but the CCJ has not been issued, you can still respond. Get in touch with HMCTS on {civilMoneyClaimsTelephone} if you are in England and Wales, or 0300 790 6234 if you are in Scotland. ' ||
        'You can call from Monday to Friday, between 8.30am to 5pm.</p>'||
        '<p class="govuk-body"><a href="{callChargesUrl}" rel="noopener noreferrer" class="govuk-link">Find out about call charges (opens in new tab).</a></p>'
        '<p class="govuk-body">If you do not get in touch, we will post a CCJ to you and <Name> and explain what to do next.</p>',
        '<p class="govuk-body">{applicant1PartyName} has requested CCJ against you, because the response deadline has passed.</p>'
        '<p class="govuk-body">Your online account will not be updated with the progress of the claim, and any further updates will be by post.</p>'
        '<p class="govuk-body">If your deadline has passed, but the CCJ has not been issued, you can still respond. Get in touch with HMCTS on {civilMoneyClaimsTelephone} if you are in England and Wales, or 0300 790 6234 if you are in Scotland. ' ||
        'You can call from Monday to Friday, between 8.30am to 5pm.</p>'||
        '<p class="govuk-body"><a href="{callChargesUrl}" rel="noopener noreferrer" class="govuk-link">Find out about call charges (opens in new tab).</a></p>'
        '<p class="govuk-body">If you do not get in touch, we will post a CCJ to you and <Name> and explain what to do next.</p>',
'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_CLAIM_URL} rel="noopener noreferrer" class="govuk-link">View the claim</a>', 'The claim','<a href={VIEW_CLAIM_URL}>View the claim</a>',
        'The claim', 'Claim.View', 'Scenario.AAA7.ClaimantIntent.CCJ.Requested.Defendant', '{3, 3}', 'DEFENDANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT} rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>', 'The claim','<a href={VIEW_INFO_ABOUT_CLAIMANT_URL}>View information about the claimant</a>',
        'The claim', 'Claim.Claimant.Info', 'Scenario.AAA7.ClaimantIntent.CCJ.Requested.Defendant', '{3, 3}', 'DEFENDANT', 2),
       ('<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response','<a href={VIEW_INFO_ABOUT_DEFENDANT_URL}>View information about the defendant</a>',
        'The response', 'Response.Defendant.Info', 'Scenario.AAA7.ClaimantIntent.CCJ.Requested.Defendant', '{3, 3}', 'DEFENDANT', 4),
       ('<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,'<a href={VIEW_ORDERS_AND_NOTICES_URL}>View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA7.ClaimantIntent.CCJ.Requested.Defendant', '{3, 3}', 'DEFENDANT', 10);
