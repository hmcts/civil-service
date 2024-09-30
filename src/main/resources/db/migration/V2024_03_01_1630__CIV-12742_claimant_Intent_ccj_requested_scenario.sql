/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.CCJ.Requested.Claimant', '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimantIntent.FullAdmit.Claimant", "Notice.AAA6.ClaimIssue.Response.Await"}',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant" : ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant', 'County Court Judgment (CCJ) requested', 'Cais am Ddyfarniad Llys Sirol (CCJ) wedi’i wneud',
        '<p class="govuk-body">We''ll process your request and post a copy of the judgment to you and ${respondent1PartyName}. We aim to do this as soon as possible.</p>'
        '<p class="govuk-body">Your online account will not be updated, and ${respondent1PartyName} will no longer be able to respond to your claim online. Any further updates will be by post.</p>'
        '<p class="govuk-body">If a postal response is received before the judgment is issued, your request will be rejected.</p>'
        '<p class="govuk-body"><a href="{enforceJudgementUrl}" target="_blank" rel="noopener noreferrer" class="govuk-link">Find out about actions you can take once a CCJ is issued (opens in a new tab)</a>.</p>',
        '<p class="govuk-body">Byddwn yn prosesu eich cais ac yn anfon copi o’r dyfarniad drwy’r post atoch chi a ${respondent1PartyName}. Fe ymdrechwn at wneud hyn cyn gynted â phosibl.</p>'
        '<p class="govuk-body">Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru, ac ni fydd ${respondent1PartyName} yn gallu ymateb i’ch hawliad ar-lein mwyach. Byddwch yn cael eich hysbysu drwy’r post am unrhyw ddiweddariadau pellach.</p>'
        '<p class="govuk-body">Os derbynnir ymateb drwy’r post cyn cyhoeddi’r dyfarniad, bydd eich cais yn cael ei wrthod.</p>'
        '<p class="govuk-body"><a href="{enforceJudgementUrl}" target="_blank" rel="noopener noreferrer" class="govuk-link">Gwybodaeth am y camau y gallwch eu cymryd yn dilyn cyhoeddi CCJ (yn agor mewn tab newydd)</a>.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_CLAIM_URL} rel="noopener noreferrer" class="govuk-link">View the claim</a>', 'The claim',
        '<a href={VIEW_CLAIM_URL} rel="noopener noreferrer" class="govuk-link">Gweld yr hawliad</a>',
        'Yr hawliad', 'Claim.View', 'Scenario.AAA6.ClaimantIntent.CCJ.Requested.Claimant', '{3, 3}', 'CLAIMANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT} rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>', 'The claim',
        '<a href={VIEW_INFO_ABOUT_CLAIMANT} rel="noopener noreferrer" class="govuk-link">Gweld gwybodaeth am yr hawliad</a>',
        'Yr hawliad', 'Claim.Claimant.Info', 'Scenario.AAA6.ClaimantIntent.CCJ.Requested.Claimant', '{3, 3}', 'CLAIMANT', 2),
       ('<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response',
        '<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">Gweld gwybodaeth am y diffynnydd</a>',
        'Yr ymateb', 'Response.Defendant.Info', 'Scenario.AAA6.ClaimantIntent.CCJ.Requested.Claimant', '{3, 3}', 'CLAIMANT', 4),
       ('<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>',
        'Gorchmynion a rhybuddion gan y llys', 'Order.View', 'Scenario.AAA6.ClaimantIntent.CCJ.Requested.Claimant', '{3, 3}', 'CLAIMANT', 14);
