
/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant',
        '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant"}',
        '{"Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant',
        'A judgment against the defendant has been made',
        'A judgment against the defendant has been made',
        '<p class="govuk-body">The defendant should now pay you according to the terms of the judgment. <br> Once they do, you should confirm that they’ve paid you the full amount that you’re owed.<br>If they do not pay you by the date on the judgment, you can ask for enforcement action to be taken against them. <br>If you need to change the terms of payment within the judgment, such as the instalments you had previously agreed, you can make an application to vary the judgment.</p>',
        '<p class="govuk-body">The defendant should now pay you according to the terms of the judgment. <br> Once they do, you should confirm that they’ve paid you the full amount that you’re owed.<br>If they do not pay you by the date on the judgment, you can ask for enforcement action to be taken against them. <br>If you need to change the terms of payment within the judgment, such as the instalments you had previously agreed, you can make an application to vary the judgment.</p>',
        'CLAIMANT');
