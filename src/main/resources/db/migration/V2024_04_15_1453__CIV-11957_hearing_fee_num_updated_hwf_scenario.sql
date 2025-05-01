/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingFee.HWF.Updated', '{"Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant", "Notice.AAA6.CP.HearingFee.HWF.InvalidRef"}',
        '{"Notice.AAA6.CP.HearingFee.HWF.Updated": ["hearingDueDateEn", "hearingDueDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.HearingFee.HWF.Updated', 'Your help with fees application has been updated', 'Mae eich cais am help i dalu ffioedd wedi cael ei ddiweddaru',
        '<p class="govuk-body">You''ve applied for help with the hearing fee. You''ll receive an update from us within 5 to 10 working days.</p>',
        '<p class="govuk-body">Fe wnaethoch chi gais am help i dalu ffi''r gwrandawiad. Byddwch yn cael diweddariad gennym mewn 5 i 10 diwrnod gwaith.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.HearingFee.HWF.Updated', '{6, 6}', 'CLAIMANT', 9, 'Deadline is 12am on ${hearingDueDateEn}', 'y dyddiad cau yw 12am ar ${hearingDueDateCy}');
