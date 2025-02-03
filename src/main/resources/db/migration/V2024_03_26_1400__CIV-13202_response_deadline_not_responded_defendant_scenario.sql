/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefResponse.ResponseTimeElapsed.Defendant',
        '{"Notice.AAA6.DefResponse.MoreTimeRequested.Defendant", "Notice.AAA6.ClaimIssue.Response.Required", "Notice.AAA6.ClaimIssue.HWF.PhonePayment"}',
        '{"Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant" : ["applicant1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefResponse.ResponseTimeElapsed.Defendant',
        'Response to the claim', 'Ymateb i’r hawliad',
        '<p class="govuk-body">You have not responded to the claim.</p>'
        '<p class="govuk-body">${applicant1PartyName} can now request a county court judgment. You can still respond to the claim before they ask for a judgment.</p>'
        '<p class="govuk-body">A County Court Judgment can mean you find it difficult to get credit, like a mortgage or mobile phone contact. Bailiffs could also be sent to your home.</p>'
        '<p class="govuk-body"><a href="{RESPONSE_TASK_LIST_URL}" class="govuk-link">Respond to claim</a></p>',
        '<p class="govuk-body">Nid ydych wedi ymateb i’r hawliad.</p>'
        '<p class="govuk-body">Gall ${applicant1PartyName} nawr wneud cais am ddyfarniad llys sirol. Gallwch dal ymateb i’r hawliad cyn iddynt wneud cais am ddyfarniad.</p>'
        '<p class="govuk-body">Gall Dyfarniad Llys Sirol olygu eich bod yn ei chael hi''n anodd cael credyd, fel morgais neu gontract ffôn symudol. Gallai beilïaid hefyd gael eu hanfon i''ch cartref.</p>'
        '<p class="govuk-body"><a href="{RESPONSE_TASK_LIST_URL}" class="govuk-link">Ymateb i hawliad</a></p>',
        'DEFENDANT');
