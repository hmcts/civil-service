/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingFee.HWF.FullRemission',
        '{"Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant", "Notice.AAA6.CP.HearingFee.HWF.InvalidRef", "Notice.AAA6.CP.HearingFee.HWF.InfoRequired", "Notice.AAA6.CP.HearingFee.HWF.Updated" }',
        '{"Notice.AAA6.CP.HearingFee.HWF.FullRemission": ["hearingFee"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.HearingFee.HWF.FullRemission', 'Your help with fees application has been approved',
        'Mae eich cais am help gyda ffioedd wedi''i gymeradwyo',
        '<p class="govuk-body">The full hearing fee of ${hearingFee} will be covered by fee remission. You do not need to make a payment.</p>',
        '<p class="govuk-body">Telir ffi lawn y gwrandawiad o ${hearingFee}. Nid oes angen i chi wneud taliad.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.HearingFee.HWF.FullRemission', '{7, 7}', 'CLAIMANT', 9, NULL, NULL);


