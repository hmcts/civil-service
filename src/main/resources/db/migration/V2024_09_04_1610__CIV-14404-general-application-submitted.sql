/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.ApplicationSubmitted.Applicant',
        '{"Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant","Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant","Notice.AAA6.GeneralApps.HwFRequested.Applicant","Notice.AAA6.GeneralApps.HwFRejected.Applicant","Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant","Notice.AAA6.GeneralApps.HwF.InvalidRef.Applicant","Notice.AAA6.GeneralApps.HwF.Updated.Applicant","Notice.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant"}',
        '{"Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant',
        'Application is being processed',
        'Mae’r cais yn cael ei brosesu',
        '<p class="govuk-body"> A judge will consider the application. </p>' ||
        '<p class="govuk-body"> The other parties can respond within 5 working days after the application is submitted, unless you''ve chosen not to inform them. If you have a hearing in the next 10 days, your application will be treated urgently.</p>' ||
        ' <p> <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">View application documents</a>.</p>',
        '<p class="govuk-body"> Bydd barnwr yn ystyried y cais. </p>' ||
        '<p class="govuk-body"> Gall y partïon eraill ymateb o fewn 5 diwrnod gwaith ar ôl i’r cais gael ei gyflwyno, oni bai eich bod wedi dewis peidio â rhoi gwybod iddynt. Os oes gennych wrandawiad o fewn y 10 diwrnod nesaf, bydd eich cais yn cael ei drin ar frys. </p>' ||
        ' <p> <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Gweld dogfennau’r cais</a>.</p>',
        'APPLICANT');
