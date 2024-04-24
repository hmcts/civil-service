/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant',
        '{"Notice.AAA6.CP.HearingFee.Required.Claimant"}',
        '{"Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant":[]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant',
        'We''re reviewing your help with fees application' , 'We''re reviewing your help with fees application',
        '<p class="govuk-body">You''ve applied for help with the hearing fee. You''ll receive an update in 5 to 10 working days.</p>',
        '<p class="govuk-body">You''ve applied for help with the hearing fee. You''ll receive an update in 5 to 10 working days.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy,
                                    template_name, scenario_name, task_status_sequence, role, task_order)
values ('<a>Pay the hearing fee</a>', 'Hearings' ,
        '<a>Pay the hearing fee</a>', 'Hearings',
        'Hearing.View', 'Scenario.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant',
        '{6, 6}', 'CLAIMANT', 11);
