/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant"}',
        '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Defendant": ["defendantAdmittedAmount", "paymentAgreement"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant',
        'A judgment has been made against you',
        'A judgment has been made against you',
        '<p class="govuk-body">The judgment formalises the payment plan you’ve agreed with the claimant.<br>You’ve agreed to pay the claim amount of £${defendantAdmittedAmount} ${paymentAgreement}.<br>The claimant’s details for payment and the full payment plan can be found on the judgment.<br>If you can no longer afford the repayments you’ve agreed with the claimant, you can <u>make an application to vary the judgment</u>.</p>',
        '<p class="govuk-body">The judgment formalises the payment plan you’ve agreed with the claimant.<br>You’ve agreed to pay the claim amount of £${defendantAdmittedAmount} ${paymentAgreement}.<br>The claimant’s details for payment and the full payment plan can be found on the judgment.<br>If you can no longer afford the repayments you’ve agreed with the claimant, you can <u>make an application to vary the judgment</u>.</p>',
        'DEFENDANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View hearings</a>', 'Hearing',
        '<a>Gweld y gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Llwytho dogfennau''r gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 10),
       ('<a>View documents</a>', 'Hearing',
        '<a>Gweld y dogfennau</a>', 'Gwrandawiad',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 11),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Ychwanegu trefniadau''r treial</a>', 'Gwrandawiad',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 12),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>Gweld y bwndel</a>', 'Gwrandawiad',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{2, 2}', 'DEFENDANT', 13),
       ('<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court',
        '<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>', 'Gorchmynion a rhybuddion gan y llys',
        'Order.View', 'Scenario.AAA6.ClaimIssue.ClaimSubmit.Required', '{3, 3}', 'DEFENDANT', 14),
       ('<a href={VIEW_JUDGEMENT} class="govuk-link">View the judgment</a>', 'Judgments from the court',
        '<a href={VIEW_JUDGEMENT} class="govuk-link">Gweld y Dyfarniad</a>', 'Dyfarniad gan y llys',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant', '{3, 3}', 'DEFENDANT', 15);
