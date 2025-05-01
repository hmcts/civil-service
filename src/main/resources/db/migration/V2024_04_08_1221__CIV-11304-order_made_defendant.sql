/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.OrderMade.Defendant', '{}', '{"Notice.AAA6.CP.OrderMade.Defendant" : ["orderDocument"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.OrderMade.Defendant', 'An order has been made', 'Mae gorchymyn wedi’i wneud',
        '<p class="govuk-body">The judge has made an order on your claim.</p><p class="govuk-body"><a href="{VIEW_FINAL_ORDER}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the order</a></p>',
        '<p class="govuk-body">Mae''r Barnwr wedi gwneud gorchymyn ar eich hawliad.</p><p class="govuk-body"><a href="{VIEW_FINAL_ORDER}" rel="noopener noreferrer" target="_blank" class="govuk-link">Gweld y gorchymyn</a></p>',
        'DEFENDANT', 'Session');
