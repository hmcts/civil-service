/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant',
        '{"Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantRejectsDefPlan.CourtAgreesWithDef.Defendant ", "Notice.AAA6.ClaimantIntent.ClaimantRejectsPlan.DefendantOrgLtdCo.Defendant", "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantRejectsDefPlan.CourtAgreesWithClaimant.Defendant"}',
        '{"Notice.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant": ["paymentFrequencyMessage"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant',
        'A judgment by determination has been made against you',
        'A judgment by determination has been made against you',
        '<p class="govuk-body">The instalments you need to pay have been decided by the court. They made a ‘determination of means’ and have determined an instalment amount that you can afford. <br> ${paymentFrequencyMessage}.<br> The claimant’s details for payment and the full payment plan can be found on <a href="{VIEW_JUDGEMENT}" class="govuk-link">the judgment.</a>.<br> If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can <a href="{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}" class="govuk-link">make an application to set aside (remove) or vary the judgment.</a></p>',
        '<p class="govuk-body">The instalments you need to pay have been decided by the court. They made a ‘determination of means’ and have determined an instalment amount that you can afford. <br> ${paymentFrequencyMessage}.<br> The claimant’s details for payment and the full payment plan can be found on <a href="{VIEW_JUDGEMENT}" class="govuk-link">the judgment.</a>.<br> If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can <a href="{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}" class="govuk-link">make an application to set aside (remove) or vary the judgment.</a></p>',
        'DEFENDANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a href={VIEW_CLAIM_URL} class="govuk-link">View the claim</a>', 'The claim',
        '<a href={VIEW_CLAIM_URL} class="govuk-link">View the claim</a>', 'The claim',
        'Claim.View', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{3, 3}', 'DEFENDANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">View information about the claimant</a>', 'The claim',
        '<a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">View information about the claimant</a>', 'The claim',
        'Claim.Claimant.Info', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{3, 3}', 'DEFENDANT', 2),
       ('<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>', 'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View the response to the claim</a>', 'The response',
        'Response.View', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{3, 3}', 'DEFENDANT', 3),
       ('<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response',
        '<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response',
        'Response.Defendant.Info', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{3, 3}', 'DEFENDANT', 4),
       ('<a>View hearings</a>', 'Hearing',
        '<a>View hearings</a>', 'Hearing',
        'Hearing.View', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{2, 2}', 'DEFENDANT', 5),
       ('<a>Upload hearing documents</a>', 'Hearing',
        '<a>Upload hearing documents</a>', 'Hearing',
        'Hearing.Document.Upload', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{2, 2}', 'DEFENDANT', 6),
       ('<a>View documents</a>', 'Hearing',
        '<a>View documents</a>', 'Hearing',
        'Hearing.Document.View', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{2, 2}', 'DEFENDANT', 7),
       ('<a>Add the trial arrangements</a>', 'Hearing',
        '<a>Add the trial arrangements</a>', 'Hearing',
        'Hearing.Arrangements.Add', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{2, 2}', 'DEFENDANT', 8),
       ('<a>View the bundle</a>', 'Hearing',
        '<a>View the bundle</a>', 'Hearing',
        'Hearing.Bundle.View', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{2, 2}', 'DEFENDANT', 10),
       ('<a>View the judgment</a>', 'Judgments from the court',
        '<a>View the judgment</a>', 'Judgments from the court',
        'Judgment.View', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{1, 1}', 'DEFENDANT', 14),
       ('<a href={VIEW_ORDERS_AND_NOTICES} class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES} class="govuk-link">View orders and notices</a>', 'Orders and notices from the court',
        'Order.View', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{3, 3}', 'DEFENDANT', 13),
       ('<a>View applications</a>', 'Applications' ,'<a>View applications</a>',
        'Applications', 'Application.View', 'Scenario.AAA6.JudgmentsOnline.JudgmentDeterminationIssued.Defendant', '{3, 3}', 'DEFENDANT', 15);
