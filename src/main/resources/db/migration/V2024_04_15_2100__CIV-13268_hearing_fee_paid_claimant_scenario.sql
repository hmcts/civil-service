/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingFee.Paid.Claimant',
        '{"Notice.AAA6.CP.HearingFee.Required.Claimant", "Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant", "Notice.AAA6.CP.HearingFee.HWF.InvalidRef", "Notice.AAA6.CP.HearingFee.HWF.Updated", "Notice.AAA6.CP.HearingFee.HWF.InfoRequired", "Notice.AAA6.CP.HearingFee.HWF.PartRemission", "Notice.AAA6.CP.HearingFee.HWF.Rejected"}',
        '{"Notice.AAA6.CP.HearingFee.Paid.Claimant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.CP.HearingFee.Paid.Claimant',
        'The hearing fee has been paid',
        'Mae ffi’r gwrandawiad wedi’i thalu',
        '<p class="govuk-body">The hearing fee has been paid in full.</p>',
        '<p class="govuk-body">Mae ffi’r gwrandawiad wedi’i thalu’n llawn.</p>',
        'CLAIMANT');


/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values ('<a>Pay the hearing fee</a>', 'Hearing' ,'<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.HearingFee.Paid.Claimant', '{7, 7}', 'CLAIMANT', 9, null, null);
