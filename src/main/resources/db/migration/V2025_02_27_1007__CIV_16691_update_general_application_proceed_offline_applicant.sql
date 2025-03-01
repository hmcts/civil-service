/**
 * Add scenario for applicant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Update.GeneralApps.ApplicationProceedsOffline.Applicant',
        '{"Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant",
          "Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant",
          "Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant",
          "Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant",
          "Notice.AAA6.GeneralApps.OrderMade.Applicant",
          "Notice.AAA6.GeneralApps.HearingScheduled.Applicant",
          "Notice.AAA6.GeneralApps.MoreInfoRequired.Applicant",
          "Notice.AAA6.GeneralApps.OtherPartyUploadedDocuments.Applicant",
          "Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Applicant",
          "Notice.AAA6.GeneralApps.HwFRequested.Applicant",
          "Notice.AAA6.GeneralApps.HwFRejected.Applicant",
          "Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant",
          "Notice.AAA6.GeneralApps.HwF.InvalidRef.Applicant",
          "Notice.AAA6.GeneralApps.HwF.Updated.Applicant",
          "Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant",
          "Notice.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant",
          "Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant"}',
        '{"Notice.AAA6.GeneralApps.ApplicationProceedsOffline.Applicant": []}');
