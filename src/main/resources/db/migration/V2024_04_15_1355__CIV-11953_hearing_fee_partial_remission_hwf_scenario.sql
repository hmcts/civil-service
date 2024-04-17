/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingFee.HWF.PartRemission', '{"Notice.AAA6.CP.HearingFee.HWF.Requested"}',
        '{"Notice.AAA6.CP.HearingFee.HWF.PartRemission" : ["hearingFeeRemissionAmount","hearingFeeOutStandingAmount"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.CP.HearingFee.HWF.PartRemission',
        'Your help with fees application has been reviewed',
        'Your help with fees application has been reviewed',
        '<p class="govuk-body">You''ll get help with the hearing fee. You''ll receive ${hearingFeeRemissionAmount} towards it. You must still pay the remaining fee of ${hearingFeeOutStandingAmount}. You can pay by phone by calling {civilMoneyClaimsTelephone}. If you do not pay, your claim will be struck out.</p>',
        '<p class="govuk-body">You''ll get help with the hearing fee. You''ll receive ${hearingFeeRemissionAmount} towards it. You must still pay the remaining fee of ${hearingFeeOutStandingAmount}. You can pay by phone by calling {civilMoneyClaimsTelephone}. If you do not pay, your claim will be struck out.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a>Pay the hearing fee</a>', 'Hearings' ,'<a>Pay the hearing fee</a>',
        'Hearings', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.HearingFee.HWF.PartRemission', '{5, 5}', 'CLAIMANT', 8, NULL, NULL);
