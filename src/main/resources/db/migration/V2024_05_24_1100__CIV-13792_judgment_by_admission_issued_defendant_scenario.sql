/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant"}',
        '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Defendant": ["ccjDefendantAdmittedAmount", "ccjPaymentMessageEn", "ccjPaymentMessageCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.IssuedCCJ.Defendant',
        'A judgment has been made against you',
        'Mae Dyfarniad wedi’i wneud yn eich erbyn',
        '<p class="govuk-body">The judgment formalises the payment plan you’ve agreed with the claimant.<br>You’ve agreed to pay the claim amount of £${ccjDefendantAdmittedAmount} ${ccjPaymentMessageEn}. <br>The claimant’s details for payment and the full payment plan can be found on the judgment.<br>If you can no longer afford the repayments you’ve agreed with the claimant, you can <u>make an application to vary the judgment</u>.</p>',
        '<p class="govuk-body">Mae’r dyfarniad yn ffurfioli’r cynllun taliadau yr ydych wedi cytuno arno gyda’r hawlydd.<br>Rydych wedi cytuno i dalu’ swm yr hawliad, sef £${ccjDefendantAdmittedAmount} ${ccjPaymentMessageCy}. <br>Gellir dod o hyd i fanylion yr hawlydd ar gyfer talu a’r cynllun talu llawn ar y dyfarniad.<br>Os na allwch fforddio i dalu’r rhandaliadau rydych wedi cytuno arnynt gyda’r hawlydd, gallwch <u>wneud cais i amrywio’r dyfarniad</u>.</p>',
        'DEFENDANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View the hearing</a>', 'Hearing',
        '<a>Gweld y gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant', '{2, 2}', 'DEFENDANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Llwytho dogfennau''r gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant', '{2, 2}', 'DEFENDANT', 9),
       ('<a>View documents</a>', 'Hearing',
        '<a>Gweld y dogfennau</a>', 'Gwrandawiad',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant', '{2, 2}', 'DEFENDANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Ychwanegu trefniadau''r treial</a>', 'Gwrandawiad',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant', '{2, 2}', 'DEFENDANT', 11),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>Gweld y bwndel</a>', 'Gwrandawiad',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant', '{2, 2}', 'DEFENDANT', 12),
       ('<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court',
        '<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>', 'Gorchmynion a rhybuddion gan y llys',
        'Order.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant', '{3, 3}', 'DEFENDANT', 13),
       ('<a href={VIEW_JUDGEMENT} class="govuk-link">View the judgment</a>', 'Judgments from the court',
        '<a href={VIEW_JUDGEMENT} class="govuk-link">Gweld y Dyfarniad</a>', 'Dyfarniadau gan y llys',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant', '{3, 3}', 'DEFENDANT', 14),
       ('<a href={COMFIRM_YOU_PAID_JUDGMENT_DEBT} class="govuk-link">Confirm you''ve paid a judgment (CCJ) debt</a>', 'Judgments from the court',
        '<a href={COMFIRM_YOU_PAID_JUDGMENT_DEBT} class="govuk-link">Cadarnhewch eich bod wedi talu dyled dyfarniad (CCJ)</a>', 'Dyfarniadau gan y llys',
        'Judgment.Cosc', 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant', '{3, 3}', 'DEFENDANT', 15);
