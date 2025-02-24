/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.ApplicationProceedsOffline.Applicant',
        '{Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant}',
        '{"Notice.AAA6.GeneralApps.ApplicationProceedsOffline.Applicant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.ApplicationProceedsOffline.Applicant',
        'This application continues offline',
        'Mae''r cais hwn yn parhau all-lein',
        '<p class="govuk-body">The application will continue offline. There will not be any further updates here. All updates will be by post.</p><p class="govuk-body">If the application has not yet been paid for, you will need to submit it to the court using <a href="{MAKE_APPLICATION_TO_COURT_URL}" class="govuk-link">form N244</a> together with your Help with Fees number if appropriate.</p>',
        '<p class="govuk-body">Bydd y cais yn parhau all-lein. Ni fydd unrhyw ddiweddariadau pellach yma. Bydd yr holl ddiweddariadau yn digwydd drwy''r post.</p><p class="govuk-body">Os nad ydych eisoes wedi talu am y cais, bydd angen i chi ei gyflwyno i''r llys gan ddefnyddio <a href="{MAKE_APPLICATION_TO_COURT_URL}" class="govuk-link">ffurflen N244</a> ynghyd â''ch rhif Help i Dalu Ffioedd os yw''n briodol.</p>',
        'CLAIMANT');
