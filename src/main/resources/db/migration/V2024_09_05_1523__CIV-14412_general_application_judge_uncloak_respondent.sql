/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.ApplicationUncloaked.OrderMade.Respondent',
        '{}',
        '{"Notice.AAA6.GeneralApps.ApplicationUncloaked.OrderMade.Respondent": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.ApplicationUncloaked.OrderMade.Respondent',
        'An order has been made',
        'Mae gorchymyn wedi’i wneud',
        '<p class="govuk-body">The other parties have requested a change to the case and the judge has made an order.</p><a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link">View the request and order from the judge</a>',
        '<p class="govuk-body">Mae’r partïon eraill wedi gofyn i newid gael ei wneud i''r achos ac mae’r barnwr wedi gwneud gorchymyn.</p><a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link">Gweld y cais a’r gorchymyn gan y barnwr</a>',
        'RESPONDENT');
