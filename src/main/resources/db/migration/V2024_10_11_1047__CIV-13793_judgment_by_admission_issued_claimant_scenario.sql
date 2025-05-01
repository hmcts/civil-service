/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Claimant"}',
        '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Claimant": ["djClaimantNotificationMessage", "djClaimantNotificationMessageCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.IssuedCCJ.Claimant',
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
VALUES ('<a>View the hearing</a>', 'Hearing',
        '<a>Gweld y gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Llwytho dogfennau''r gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 9),
       ('<a>View documents</a>', 'Hearing',
        '<a>Gweld y dogfennau</a>', 'Gwrandawiad',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Ychwanegu trefniadau''r treial</a>', 'Gwrandawiad',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 11),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>Gweld y bwndel</a>', 'Gwrandawiad',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 12),
       ('<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court',
        '<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>', 'Gorchmynion a rhybuddion gan y llys',
        'Order.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'CLAIMANT', 13),
       ('<a href={VIEW_JUDGEMENT} class="govuk-link">View the judgment</a>', 'Judgments from the court',
        '<a href={VIEW_JUDGEMENT} class="govuk-link">Gweld y Dyfarniad</a>', 'Dyfarniadau gan y llys',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'CLAIMANT', 14),
       ('<a href={COMFIRM_YOU_PAID_JUDGMENT_DEBT} class="govuk-link">Confirm you''ve paid a judgment (CCJ) debt</a>', 'Judgments from the court',
        '<a href={COMFIRM_YOU_PAID_JUDGMENT_DEBT} class="govuk-link">Cadarnhewch eich bod wedi talu dyled dyfarniad (CCJ)</a>', 'Dyfarniadau gan y llys',
        'Judgment.Cosc', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'CLAIMANT', 15);
