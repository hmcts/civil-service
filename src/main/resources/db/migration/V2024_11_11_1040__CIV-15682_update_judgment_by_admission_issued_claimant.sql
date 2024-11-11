/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Claimant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Claimant"}',
        '{"Notice.AAA6.JudgmentsOnline.IssuedCCJ.Claimant": ["djClaimantNotificationMessage", "djClaimantNotificationMessageCy"]}');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View the hearing</a>', 'Hearing',
        '<a>Gweld y gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.View', 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 8),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Llwytho dogfennau''r gwrandawiad</a>', 'Gwrandawiad',
        'Hearing.Document.Upload', 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 9),
       ('<a>View documents</a>', 'Hearing',
        '<a>Gweld y dogfennau</a>', 'Gwrandawiad',
        'Hearing.Document.View', 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Ychwanegu trefniadau''r treial</a>', 'Gwrandawiad',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 11),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>Gweld y bwndel</a>', 'Gwrandawiad',
        'Hearing.Bundle.View', 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant', '{2, 2}', 'CLAIMANT', 12),
       ('<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court',
        '<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>', 'Gorchmynion a rhybuddion gan y llys',
        'Order.View', 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'CLAIMANT', 13),
       ('<a href={VIEW_JUDGEMENT} class="govuk-link">View the judgment</a>', 'Judgments from the court',
        '<a href={VIEW_JUDGEMENT} class="govuk-link">Gweld y Dyfarniad</a>', 'Dyfarniadau gan y llys',
        'Judgment.View', 'Scenario.AAA6.Update.JudgmentsOnline.IssuedCCJ.Claimant', '{3, 3}', 'CLAIMANT', 14);
