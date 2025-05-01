/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingFee.HWF.PartRemission', '{"Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant"}',
        '{"Notice.AAA6.CP.HearingFee.HWF.PartRemission" : ["hearingFeeRemissionAmount","hearingFeeOutStandingAmount", "hearingDueDateEn", "hearingDueDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.CP.HearingFee.HWF.PartRemission',
        'Your help with fees application has been reviewed',
        'Mae eich cais am help i dalu ffioedd wedi cael ei adolygu',
        '<p class="govuk-body">You''ll get help with the hearing fee. ${hearingFeeRemissionAmount} will be covered by fee remission. You must still pay the remaining fee of ${hearingFeeOutStandingAmount} by ${hearingDueDateEn}. You can pay by phone by calling {civilMoneyClaimsTelephone}. If you do not pay, your claim will be struck out.</p>',
        '<p class="govuk-body">Byddwch yn cael help gyda ffi''r gwrandawiad. Bydd ${hearingFeeRemissionAmount} yn cael ei gyflenwi gan ddileu ffi. Rhaid i chi dal dalu''r ffi sy''n weddill o ${hearingFeeOutStandingAmount} erbyn ${hearingDueDateCy}. Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}. Os na fyddwch yn talu, bydd eich hawliad yn cael ei ddileu.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.HearingFee.HWF.PartRemission', '{5, 5}', 'CLAIMANT', 9, 'Deadline is 12am on ${hearingDueDateEn}', 'y dyddiad cau yw 12am ar ${hearingDueDateCy}');
