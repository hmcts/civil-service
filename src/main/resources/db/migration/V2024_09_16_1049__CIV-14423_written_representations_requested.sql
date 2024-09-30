/**
 * Add scenarios
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.WrittenRepresentationRequired.Applicant', '{"Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant","Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant","Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant","Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant"}', '{"Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Applicant": ["writtenRepApplicantDeadlineDateEn", "writtenRepApplicantDeadlineDateCy"]}');

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.WrittenRepresentationRequired.Respondent', '{"Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Respondent","Notice.AAA6.GeneralApps.UrgentApplicationMade.Respondent","Notice.AAA6.GeneralApps.NonUrgentApplicationMade.Respondent"}', '{"Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Respondent": ["writtenRepRespondentDeadlineDateEn", "writtenRepRespondentDeadlineDateCy"]}');

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.DeleteWrittenRepresentationRequired.Applicant', '{"Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Applicant"}', '{}');

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.DeleteWrittenRepresentationRequired.Respondent', '{"Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Respondent"}', '{}');

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.SwitchWrittenRepresentationRequired.RespondentApplicant', '{"Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Respondent","Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant","Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant","Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant","Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant"}', '{"Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Applicant": ["writtenRepApplicantDeadlineDateEn", "writtenRepApplicantDeadlineDateCy"]}');


INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Applicant', 'You need to provide written representation', 'Mae angen i chi ddarparu cynrychiolaeth ysgrifenedig',
        '<p class="govuk-body">The court has requested that you must <a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">provide written representation</a>. You must do this by 4pm on ${writtenRepApplicantDeadlineDateEn}.</p>',
        '<p class="govuk-body">Mae''r llys wedi gofyn i chi <a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">ddarparu cynrychiolaeth ysgrifenedig</a>. Rhaid i chi wneud hyn erbyn 4pm ar ${writtenRepApplicantDeadlineDateCy}.</p>',
        'APPLICANT');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Respondent', 'You need to provide written representation', 'Mae angen i chi ddarparu cynrychiolaeth ysgrifenedig',
        '<p class="govuk-body">The court has requested that you must <a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">provide written representation</a>. You must do this by 4pm on ${writtenRepRespondentDeadlineDateEn}.</p>',
        '<p class="govuk-body">Mae''r llys wedi gofyn i chi <a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">ddarparu cynrychiolaeth ysgrifenedig</a>. Rhaid i chi wneud hyn erbyn 4pm ar ${writtenRepRespondentDeadlineDateCy}.</p>',
        'RESPONDENT');
