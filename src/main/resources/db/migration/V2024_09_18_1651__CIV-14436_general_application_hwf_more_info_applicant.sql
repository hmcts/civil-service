/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant',
        '{"Notice.AAA6.GeneralApps.HwFRequested.Applicant", "Notice.AAA6.GeneralApps.HwF.Updated.Applicant"}',
        '{"Notice.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant" : ["applicationFee", "applicationFeeTypeEn", "applicationFeeTypeCy"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant',
        'Your help with fees application needs more information',
        'Mae angen i chi ddarparu mwy o wybodaeth ar gyfer eich cais am help i dalu ffioedd',
        '<p class="govuk-body">We need more information on your application for help with the ${applicationFeeTypeEn} fee.</p><p class="govuk-body">You’ve been sent an email with further details. If you’ve already read the email and taken action, you can disregard this message.</p><p class="govuk-body">You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">Mae arnom angen mwy o wybodaeth ar gyfer eich cais am help i dalu''r ffi gwneud ${applicationFeeTypeCy}.</p><p class="govuk-body">Anfonwyd e-bost atoch gyda rhagor o fanylion. Os ydych wedi darllen yr e-bost yn barod ac wedi gweithredu, gallwch anwybyddu''r neges hon.</p><p class="govuk-body">Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>',
        'APPLICANT');
