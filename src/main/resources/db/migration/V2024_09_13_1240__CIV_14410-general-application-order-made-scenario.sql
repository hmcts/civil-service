/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.OrderMade.Applicant',
        '{"Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant",
          "Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant",
          "Notice.AAA6.GeneralApps.HearingScheduled.Applicant",
          "Notice.AAA6.GeneralApps.OtherPartyUploadedDocuments.Applicant",
          "Notice.AAA6.GeneralApps.DocumentsSubmitted.Applicant",
          "Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant",
          "Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant"}',
        '{"Notice.AAA6.GeneralApps.OrderMade.Applicant": []}');

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.OrderMade.Respondent',
        '{"Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Respondent",
          "Notice.AAA6.GeneralApps.UrgentApplicationMade.Respondent",
          "Notice.AAA6.GeneralApps.HearingScheduled.Respondent",
          "Notice.AAA6.GeneralApps.OtherPartyUploadedDocuments.Respondent",
          "Notice.AAA6.GeneralApps.DocumentsSubmitted.Respondent"}',
        '{"Notice.AAA6.GeneralApps.OrderMade.Respondent": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.OrderMade.Applicant',
        'An order has been made',
        'Mae gorchymyn wedi’i wneud',
        '<p class="govuk-body">The judge has made an order related to the application. <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">View the order</a></p>',
        '<p class="govuk-body">Mae''r barnwr wedi gwneud gorchymyn yn ymwneud â''r cais. <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Gweld y gorchymyn</a></p>',
        'APPLICANT');

INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.OrderMade.Respondent',
        'An order has been made',
        'Mae gorchymyn wedi’i wneud',
        '<p class="govuk-body">The judge has made an order related to the application. <a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link">View the order</a></p>',
        '<p class="govuk-body">Mae''r barnwr wedi gwneud gorchymyn yn ymwneud â''r cais. <a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link">Gweld y gorchymyn</a></p>',
        'RESPONDENT');
