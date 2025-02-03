/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.HwFRejected.Applicant',
        '{"Notice.AAA6.GeneralApps.HwFRequested.Applicant", "Notice.AAA6.GeneralApps.HwF.InvalidRef.Applicant", "Notice.AAA6.GeneralApps.HwF.Updated.Applicant", "Notice.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant"}',
        '{"Notice.AAA6.GeneralApps.HwFRejected.Applicant" : ["applicationFee", "applicationFeeTypeEn", "applicationFeeTypeCy"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.HwFRejected.Applicant',
        'Your help with fees application has been rejected',
        'Mae eich cais am help i dalu ffioedd wedi cael ei wrthod',
        '<p class="govuk-body">We''ve rejected your application for help with the ${applicationFeeTypeEn} fee. See email for further details.</p><p class="govuk-body">To progress your application, you must <a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">pay the full fee</a> of ${applicationFee}.</p><p class="govuk-body">You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">Rydym wedi gwrthod eich cais am help i dalu''r ffi gwneud ${applicationFeeTypeCy}. Gweler yr e-bost am ragor o fanylion.</p><p class="govuk-body">I symud eich cais yn ei flaen, rhaid i chi <a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">dalu''r ffi lawn</a> o ${applicationFee}.</p><p class="govuk-body">Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>',
        'APPLICANT');
