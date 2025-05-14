/**
 * Add scenario to delete duplicate notifications
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.LiPQM.QueryResponded.Claimant.Delete',
        '{"Notice.AAA6.LiPQM.QueryResponded.Claimant"}',
        '{"": []}'),
       ('Scenario.AAA6.LiPQM.QueryResponded.Defendant.Delete',
        '{"Notice.AAA6.LiPQM.QueryResponded.Defendant"}',
        '{"": []}');

/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.LiPQM.QueryResponded.Claimant',
        '{"Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Claimant","Notice.AAA6.LiPQM.NoC.AllOpenQueriesClosed.Claimant"}',
        '{"Notice.AAA6.LiPQM.QueryResponded.Claimant": []}'),
       ('Scenario.AAA6.LiPQM.QueryResponded.Defendant',
        '{"Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Defendant","Notice.AAA6.LiPQM.NoC.AllOpenQueriesClosed.Defendant"}',
        '{"Notice.AAA6.LiPQM.QueryResponded.Defendant": []}');

/**
 * Add notification template claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role, time_to_live)
VALUES ('Notice.AAA6.LiPQM.QueryResponded.Claimant',
        'The court has responded to your message',
        'Mae’r llys wedi ymateb i’ch neges',
        '<p class="govuk-body">The court has responded to the message you sent. <br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">View the message</a></p>',
        '<p class="govuk-body">Mae''r llys wedi ymateb i''r neges a anfonwyd gennych. <br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">Gweld y neges</a></p>',
        'CLAIMANT', 'Click');

/**
 * Add notification template defendant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role, time_to_live)
VALUES ('Notice.AAA6.LiPQM.QueryResponded.Defendant',
        'The court has responded to your message',
        'Mae’r llys wedi ymateb i’ch neges',
        '<p class="govuk-body">The court has responded to the message you sent. <br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">View the message</a></p>',
        '<p class="govuk-body">Mae''r llys wedi ymateb i''r neges a anfonwyd gennych. <br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">Gweld y neges</a></p>',
        'DEFENDANT', 'Click');

