/**
 * Add scenario to delete duplicate notifications
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.LiPQM.QueryRaisedByOtherParty.Claimant.Delete',
        '{"Notice.AAA6.LiPQM.QueryRaisedByOtherParty.Claimant"}',
        '{"": []}'),
       ('Scenario.AAA6.LiPQM.QueryRaisedByOtherParty.Defendant.Delete',
        '{"Notice.AAA6.LiPQM.QueryRaisedByOtherParty.Defendant"}',
        '{"": []}');

/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.LiPQM.QueryRaisedByOtherParty.Claimant',
        '{}',
        '{"Notice.AAA6.LiPQM.QueryRaisedByOtherParty.Claimant":[]}');

/**
 * Add notification template for claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role, time_to_live)
VALUES ('Notice.AAA6.LiPQM.QueryRaisedByOtherParty.Claimant',
        'Important',
        'Pwysig',
        '<p class="govuk-body">There has been a message sent on your case. <br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">View the message</a></p>',
        '<p class="govuk-body">Mae neges wedi’i hanfon ar eich achos. <br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">Gweld y neges</a></p>',
        'CLAIMANT', 'Click');

/**
 * Add scenario for defendant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.LiPQM.QueryRaisedByOtherParty.Defendant',
        '{}',
        '{"Notice.AAA6.LiPQM.QueryRaisedByOtherParty.Defendant":[]}');

/**
 * Add notification template for defendant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role, time_to_live)
VALUES ('Notice.AAA6.LiPQM.QueryRaisedByOtherParty.Defendant',
        'Important',
        'Pwysig',
        '<p class="govuk-body">There has been a message sent on your case. <br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">View the message</a></p>',
        '<p class="govuk-body">Mae neges wedi’i hanfon ar eich achos. <br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">Gweld y neges</a></p>',
        'DEFENDANT', 'Click');
