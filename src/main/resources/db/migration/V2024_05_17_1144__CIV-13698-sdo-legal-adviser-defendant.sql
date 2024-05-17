/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.SDOMadebyLA.Defendant', '{}', '{"Notice.AAA6.CP.SDOMadebyLA.Defendant" : ["requestForReconsiderationDeadlineEn", "requestForReconsiderationDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.SDOMadebyLA.Defendant', 'An order has been made on this claim', 'An order has been made on this claim',
        '<p class="govuk-body">You need to carefully <a href="{VIEW_FINAL_ORDER}" rel="noopener noreferrer" target="_blank" class="govuk-link">read and review this order</a>. If you don’t agree with something in the order you can <a href="{REQUEST_FOR_RECONSIDERATION}" rel="noopener noreferrer" class="govuk-link">ask the court to review it</a>. You can only do this once. You will have to provide details about what changes you want made and these will be reviewed by a judge. This must be done before {requestForReconsiderationDeadlineEn}.</p>',
        '<p class="govuk-body">You need to carefully <a href="{VIEW_FINAL_ORDER}" rel="noopener noreferrer" target="_blank" class="govuk-link">read and review this order</a>. If you don’t agree with something in the order you can <a href="{REQUEST_FOR_RECONSIDERATION}" rel="noopener noreferrer" class="govuk-link">ask the court to review it</a>. You can only do this once. You will have to provide details about what changes you want made and these will be reviewed by a judge. This must be done before {requestForReconsiderationDeadlineCy}.</p>',
        'DEFENDANT');
