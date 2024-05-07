/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant"}',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant',
        'A judgment has been made against you',
        'A judgment has been made against you',
        '<p class="govuk-body">The exact details of what you need to pay, and by when, are stated on the judgment. <br> If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can <a href="{VIEW_AND_RESPOND}" class="govuk-link">make an application to set aside (remove) or vary the judgment</a>.</p>',
        '<p class="govuk-body">The exact details of what you need to pay, and by when, are stated on the judgment. <br> If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can <a href="{VIEW_AND_RESPOND}" class="govuk-link">make an application to set aside (remove) or vary the judgment</a>.</p>',
        'DEFENDANT');
