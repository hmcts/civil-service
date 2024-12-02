/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Court.Officer.Order.Defendant', '{"Notice.AAA6.CP.Hearing.Scheduled","Notice.AAA6.CP.Trial Arrangements.Required","Notice.AAA6.CP.Trial Arrangements.Finalised"}', '{"Notice.AAA6.CP.Court.Officer.Order.Defendant" : []}'),


/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.Court.Officer.Order.Defendant', 'An order has been made', 'Mae gorchymyn wedi’i wneud',
  '<p class="govuk-body">The Court has made an order on your claim.</p><p class="govuk-body"><a href="{VIEW_FINAL_ORDER}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the order</a></p>',
  '<p class="govuk-body">Mae’r Llys wedi gwneud gorchymyn ar eich hawliad.</p><p class="govuk-body"><a href="{VIEW_FINAL_ORDER}" rel="noopener noreferrer" target="_blank" class="govuk-link">Gweld y gorchymyn</a></p>',
  'DEFENDANT');

