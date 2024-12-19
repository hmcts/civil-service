/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Discontinue.NoticeOfDiscontinuanceIssued.Defendant',
        '{}',
        '{"Notice.AAA6.Discontinue.NoticeOfDiscontinuanceIssued.Defendant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy,
                                                   notification_role)
VALUES ('Notice.AAA6.Discontinue.NoticeOfDiscontinuanceIssued.Defendant',
        'A notice of discontinuance has been created and sent to all parties',
        'MMae hysbysiad o ddirwyn i ben wedi’i greu a’i anfon at yr holl bartïon',
        '<p class="govuk-body">This means that all or part of this claim has been discontinued.<br>Please review the <a href="{NOTICE_OF_DISCONTINUANCE}" target="_blank" rel="noopener noreferrer" class="govuk-link">notice of discontinuance</a> carefully.</p>',
        '<p class="govuk-body">Mae hyn yn golygu bod rhan, neu''r cyfan, o''r hawliad hwn wedi dod i ben.<br>Adolygwch yr <a href="{NOTICE_OF_DISCONTINUANCE}" target="_blank" rel="noopener noreferrer" class="govuk-link">hysbysiad o ddirwyn i ben</a> yn ofalus.</p>',
        'DEFENDANT');
