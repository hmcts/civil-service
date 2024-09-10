/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.UrgentApplicationMade.Respondent', '{}', '{"Notice.AAA6.GeneralApps.UrgentApplicationMade.Respondent" : ["generalAppNotificationDeadlineDateEn", "generalAppNotificationDeadlineDateCy"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.UrgentApplicationMade.Respondent', 'The other parties have requested a change to the case', 'Mae’r partïon eraill wedi gofyn am newid yr achos',
        '<p class="govuk-body">Their request is being treated urgently as there’s a hearing date in the next 10 days. A judge will decide what the next steps should be.</p>'
            '<p class="govuk-body">You can still <a href="{GA_RESPONDENT_INFORMATION_URL}" class="govuk-link">review the request and respond</a> by 4pm on ${generalAppNotificationDeadlineDateEn}, but a judge may have decided on the next steps before you do so.</p>',
        '<p class="govuk-body">Mae eu cais yn cael ei drin ar frys gan fod dyddiad gwrandawiad o fewn y 10 diwrnod nesaf. Bydd barnwr yn penderfynu beth ddylai''r camau nesaf fod.</p>'
           '<p class="govuk-body">Gallwch barhau i <a href="{GA_RESPONDENT_INFORMATION_URL}" class="govuk-link">adolygu’r cais ac ymateb</a> erbyn 4pm ar ${generalAppNotificationDeadlineDateCy}, ond efallai y bydd barnwr wedi penderfynu ar y camau nesaf cyn i chi wneud hynny.</p>',
        'RESPONDENT');

/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.UrgentApplicationUncloaked.Respondent', '{}', '{"Notice.AAA6.GeneralApps.UrgentApplicationUncloaked.Respondent" : ["judgeRequestMoreInfoByDateEn", "judgeRequestMoreInfoByDateCy"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.UrgentApplicationUncloaked.Respondent', 'The other parties have requested a change to the case', 'Mae’r partïon eraill wedi gofyn am newid yr achos',
        '<p class="govuk-body">Their request is being treated urgently as there’s a hearing date in the next 10 days. A judge will decide what the next steps should be.</p>'
          '<p class="govuk-body">You can still <a href="{GA_RESPONDENT_INFORMATION_URL}" class="govuk-link">review the request and respond</a> by 4pm on ${judgeRequestMoreInfoByDateEn}, but a judge may have decided on the next steps before you do so.</p>',
        '<p class="govuk-body">Mae eu cais yn cael ei drin ar frys gan fod dyddiad gwrandawiad o fewn y 10 diwrnod nesaf. Bydd barnwr yn penderfynu beth ddylai''r camau nesaf fod.</p>'
          '<p class="govuk-body">Gallwch barhau i <a href="{GA_RESPONDENT_INFORMATION_URL}" class="govuk-link">adolygu’r cais ac ymateb</a> erbyn 4pm ar ${judgeRequestMoreInfoByDateCy}, ond efallai y bydd barnwr wedi penderfynu ar y camau nesaf cyn i chi wneud hynny.</p>',
        'RESPONDENT');
