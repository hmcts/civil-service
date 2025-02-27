/**
 * Add scenario for respondent
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Update.GeneralApps.ApplicationProceedsOffline.Respondent',
        '{"Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Respondent",
          "Notice.AAA6.GeneralApps.OtherPartyUploadedDocuments.Respondent",
          "Notice.AAA6.GeneralApps.MoreInfoRequired.Respondent",
          "Notice.AAA6.GeneralApps.HearingScheduled.Respondent",
          "Notice.AAA6.GeneralApps.ApplicationUncloaked.OrderMade.Respondent",
          "Notice.AAA6.GeneralApps.NonUrgentApplicationMade.Respondent",
          "Notice.AAA6.GeneralApps.UrgentApplicationMade.Respondent",
          "Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Respondent",
          "Notice.AAA6.GeneralApps.OrderMade.Respondent"}',
        '{"Notice.AAA6.GeneralApps.ApplicationProceedsOffline.Respondent": []}');
