/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Court.Officer.Order.Claimant', '{"Notice.AAA6.CP.Hearing.Scheduled.Claimant","Notice.AAA6.CP.HearingFee.Required.Claimant","Notice.AAA6.CP.Trial.Arrangements.Required.Claimant","Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant"}', '{"Notice.AAA6.CP.Court.Officer.Order.Claimant" : []}'),
       ('Scenario.AAA6.CP.Court.Officer.Order.HearingFee.Claimant', '{"Notice.AAA6.CP.Hearing.Scheduled.Claimant","Notice.AAA6.CP.HearingFee.Required.Claimant","Notice.AAA6.CP.Trial.Arrangements.Required.Claimant","Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant"}', '{"Notice.AAA6.CP.Court.Officer.Order.Claimant" : []}'),
       ('Scenario.AAA6.CP.Court.Officer.Order.TrialReady.Claimant', '{}', '{}');


/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.Court.Officer.Order.Claimant', 'An order has been made', 'Mae gorchymyn wedi’i wneud',
  '<p class="govuk-body">The Court has made an order on your claim.</p><p class="govuk-body"><a href="{VIEW_ORDERS_AND_NOTICES}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the order</a></p>',
  '<p class="govuk-body">Mae’r Llys wedi gwneud gorchymyn ar eich hawliad.</p><p class="govuk-body"><a href="{VIEW_ORDERS_AND_NOTICES}" rel="noopener noreferrer" target="_blank" class="govuk-link">Gweld y gorchymyn</a></p>',
  'CLAIMANT');

/**
 * Add task list changes
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
VALUES ('<a>View the hearing</a>', 'Hearing', '<a>Gweld y gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.View', 'Scenario.AAA6.CP.Court.Officer.Order.Claimant', '{1, 1}', 'CLAIMANT', 8),
       ('<a>View the hearing</a>', 'Hearing', '<a>Gweld y gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.View', 'Scenario.AAA6.CP.Court.Officer.Order.HearingFee.Claimant', '{1, 1}', 'CLAIMANT', 8),
       ('<a>Pay the hearing fee</a>', 'Hearing', '<a>Talu ffi''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Fee.Pay', 'Scenario.AAA6.CP.Court.Officer.Order.HearingFee.Claimant', '{2, 2}', 'CLAIMANT', 9),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Court.Officer.Order.TrialReady.Claimant', '{1, 1}', 'CLAIMANT', 11);


