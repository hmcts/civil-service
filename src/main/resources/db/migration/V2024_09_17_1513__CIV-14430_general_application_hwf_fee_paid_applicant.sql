/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.HwF.FeePaid.Applicant',
        '{"Notice.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant", "Notice.AAA6.GeneralApps.HwF.Updated.Applicant", "Notice.AAA6.GeneralApps.HwF.InvalidRef.Applicant", "Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant", "Notice.AAA6.GeneralApps.HwFRejected.Applicant", "Notice.AAA6.GeneralApps.HwFRequested.Applicant"}',
        '{"Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant" : ["applicationFeeTypeEn", "applicationFeeTypeCy"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant',
        'The ${applicationFeeTypeEn} has been paid',
        'Mae’r ffi gwneud ${applicationFeeTypeCy} wedi cael ei thalu',
        '<p class="govuk-body">The ${applicationFeeTypeEn} has been paid in full.</p>',
        '<p class="govuk-body">Mae‘r ffi gwneud ${applicationFeeTypeCy} wedi cael ei thalu’n llawn.</p>',
        'APPLICANT');
