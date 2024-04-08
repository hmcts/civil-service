/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.OrderMade.Claimant','','{"Notice.AAA6.CP.OrderMade.Claimant" : []}'),
       ('Scenario.AAA6.CP.OrderMade.Defendant', '','{"Notice.AAA6.CP.OrderMade.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.OrderMade.Claimant', 'An order has been made', 'An order has been made',
        '<p class="govuk-body">The judge has made an order on your claim. <a href="{VIEW_ORDERS_AND_NOTICES}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the order</a>.</p>',
        '<p class="govuk-body">The judge has made an order on your claim. <a href="{VIEW_ORDERS_AND_NOTICES}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the order</a>.</p>',
        'CLAIMANT'),
       ('Scenario.AAA6.CP.OrderMade.Defendant', 'An order has been made', 'An order has been made',
        '<p class="govuk-body">The judge has made an order on your claim. <a href="{VIEW_ORDERS_AND_NOTICES}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the order</a>.</p>',
        '<p class="govuk-body">The judge has made an order on your claim. <a href="{VIEW_ORDERS_AND_NOTICES}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the order</a>.</p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA6.CP.OrderMade.Claimant', '{3, 3}', 'CLAIMANT', 10),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA6.CP.OrderMade.Defendant', '{3, 3}', 'DEFENDANT', 10);
