/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.HwF.FullRemission.Applicant',
        '{"Notice.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant", "Notice.AAA6.GeneralApps.HwF.Updated.Applicant", "Notice.AAA6.GeneralApps.HwF.InvalidRef.Applicant", "Notice.AAA6.GeneralApps.HwFRequested.Applicant"}',
        '{"Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant" : ["applicationFee", "applicationFeeTypeEn", "applicationFeeTypeCy"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant',
        'Your help with fees application has been approved',
        'Mae eich cais am help i dalu ffioedd wedi cael ei gymeradwyo',
        '<p class="govuk-body">The full ${applicationFeeTypeEn} fee of ${applicationFee} will be covered by fee remission. You do not need to make a payment.</p>',
        '<p class="govuk-body">Bydd y ffi gwneud ${applicationFeeTypeCy} llawn o ${applicationFee} yn cael ei ddileu. Nid oes angen i chi wneud taliad.</p>',
        'APPLICANT');
