/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.HwF.PartRemission.Applicant',
        '{"Notice.AAA6.GeneralApps.HwFRequested.Applicant", "Notice.AAA6.GeneralApps.HwF.InvalidRef.Applicant", "Notice.AAA6.GeneralApps.HwF.Updated.Applicant", "Notice.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant"}',
        '{"Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant" : ["applicationFeeTypeEn", "applicationFeeTypeCy", "remissionAmount", "outstandingFeeInPounds"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant',
        'Your help with fees application has been reviewed',
        'Mae eich cais am help i dalu ffioedd wedi cael ei adolygu',
        '<p class="govuk-body">You’ll get help with the ${applicationFeeTypeEn} fee. ${remissionAmount} will be covered by fee remission.</p><p class="govuk-body">To progress your application, you must  still pay the remaining fee of ${outstandingFeeInPounds}.</p><p class="govuk-body">You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">Byddwch yn cael help gyda’r ffi gwneud ${applicationFeeTypeCy}. Bydd ${remissionAmount} o’r ffi yn cael ei ddileu.</p><p class="govuk-body">I symud eich cais yn ei flaen, rhaid i chi dal dalu’r ffi sy’n weddill o ${outstandingFeeInPounds}.</p><p class="govuk-body">Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>',
        'APPLICANT');
