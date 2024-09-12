
/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant",
         "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Claimant",
         "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Claimant",
         "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Claimant"}',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant": ["djClaimantNotificationMessage", "djClaimantNotificationMessageCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant',
        'A judgment against the defendant has been made',
        'Mae dyfarniad wedi’i wneud yn erbyn y diffynnydd',
        '<p class="govuk-body">The defendant should now pay you according to the terms of the judgment. <br> Once they do, you should <a href="{CONFIRM_YOU_HAVE_BEEN_PAID_URL}" class="govuk-link">confirm that they’ve paid you the full amount that you’re owed</a>.<br>If they do not pay you by the date on the judgment, you can <u>ask for enforcement action to be taken against them</u>. <br>If you need to change the terms of payment within the judgment, such as the instalments you had previously agreed, you can ${djClaimantNotificationMessage}.</p>',
        '<p class="govuk-body">Dylai’r diffynnydd eich talu yn unol â thelerau’r dyfarniad. <br> Unwaith y byddant yn gwneud hynny, dylech <a href="{CONFIRM_YOU_HAVE_BEEN_PAID_URL}" class="govuk-link">gadarnhau eu bod wedi talu’r swm llawn sy’n ddyledus i chi</a>.<br>Os na fyddant yn eich talu erbyn y dyddiad ar y dyfarniad, gallwch <u>ofyn am gymryd camau  gorfodi yn eu herbyn</u>. <br>Os oes arnoch angen newid y telerau talu o fewn y dyfarniad, fel y rhandaliadau roeddech wedi cytuno arnynt yn flaenorol, gallwch ${djClaimantNotificationMessageCy}.</p>',
        'CLAIMANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a href={VIEW_CLAIM_URL} class="govuk-link">View the claim</a>', 'The claim',
        '<a href={VIEW_CLAIM_URL} class="govuk-link">View the claim</a>', 'The claim',
        'Claim.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'CLAIMANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">View information about the claimant</a>', 'The claim',
        '<a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">View information about the claimant</a>', 'The claim',
        'Claim.Claimant.Info', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'CLAIMANT', 2),
       ('<a>View the response to the claim</a>', 'The response',
        '<a>View the response to the claim</a>', 'The response',
        'Response.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'CLAIMANT', 3),
       ('<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response',
        '<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response',
        'Response.Defendant.Info', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'CLAIMANT', 4),
       ('<a>View hearings</a>', 'Hearing',
        '<a>View hearings</a>', 'Hearing',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 5),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Upload hearing documents</a>', 'Hearing',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 6),
       ('<a>View documents</a>', 'Hearing',
        '<a>View documents</a>', 'Hearing',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 7),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Add the trial arrangements</a>', 'Hearing',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 8),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>View the bundle</a>', 'Hearing',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a href="{VIEW_JUDGEMENT}" class="govuk-link">View the judgment</a>', 'Judgments from the court',
        '<a href="{VIEW_JUDGEMENT}" class="govuk-link">View the judgment</a>', 'Judgments from the court',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{1, 1}', 'CLAIMANT', 12),
       ('<a href={VIEW_ORDERS_AND_NOTICES} class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES} class="govuk-link">View orders and notices</a>', 'Orders and notices from the court',
        'Order.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'DEFENDANT', 13);
