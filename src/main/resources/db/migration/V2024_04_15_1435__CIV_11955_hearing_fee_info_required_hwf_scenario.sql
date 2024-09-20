/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingFee.HWF.InfoRequired', '{"Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant"}',
        '{"Notice.AAA6.CP.HearingFee.HWF.InfoRequired" : ["hearingDueDateEn", "hearingDueDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.CP.HearingFee.HWF.InfoRequired',
        'Your help with fees application needs more information',
        'Mae angen i chi nodi mwy o wybodaeth ar eich cais am help i dalu ffioedd',
        '<p class="govuk-body">We need more information on your application for help with the hearing fee. You''ve been sent an email with further details. If you''ve already read the email and taken action, you can disregard this message. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">Mae arnom angen mwy o wybodaeth am eich cais am help i dalu ffi''r gwrandawiad. Anfonwyd e-bost atoch gyda mwy o fanylion. Os ydych eisoes wedi darllen yr e-bost ac wedi gweithredu, gallwch anwybyddu''r neges hon. Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.HearingFee.HWF.InfoRequired', '{6, 6}', 'CLAIMANT', 9, 'Deadline is 12am on ${hearingDueDateEn}', 'y dyddiad cau yw 12am ar ${hearingDueDateCy}');
