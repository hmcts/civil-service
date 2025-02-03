/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.ReconDecisionMade.Claimant',
        '{"Notice.AAA6.CP.ReviewOrderRequested.Recipient.Claimant", "Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Claimant"}',
        '{"Notice.AAA6.CP.ReconDecisionMade.Claimant": []}'),
       ('Scenario.AAA6.CP.ReconDecisionMade.Defendant',
        '{"Notice.AAA6.CP.ReviewOrderRequested.Recipient.Defendant", "Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Defendant"}',
        '{"Notice.AAA6.CP.ReconDecisionMade.Defendant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.ReconDecisionMade.Claimant',
        'Request has been reviewed',
        'Adolygwyd y cais',
        '<p class="govuk-body">A judge has made a decision on the order. Carefully <a href="{VIEW_DECISION_RECONSIDERATION}" rel="noopener noreferrer" target="_blank" class="govuk-link"> read and review the decision</a>.',
        '<p class="govuk-body">Mae barnwr wedi gwneud penderfyniad ar y gorchymyn. <a href="{VIEW_DECISION_RECONSIDERATION}" rel="noopener noreferrer" target="_blank" class="govuk-link"> Darllenwch ac adolygwch y penderfyniad</a> yn ofalus.',
        'CLAIMANT', 'Click'),
       ('Notice.AAA6.CP.ReconDecisionMade.Defendant',
        'Request has been reviewed',
        'Adolygwyd y cais',
        '<p class="govuk-body">A judge has made a decision on the order. Carefully <a href="{VIEW_DECISION_RECONSIDERATION}" rel="noopener noreferrer" target="_blank" class="govuk-link"> read and review the decision</a>.',
        '<p class="govuk-body">Mae barnwr wedi gwneud penderfyniad ar y gorchymyn. <a href="{VIEW_DECISION_RECONSIDERATION}" rel="noopener noreferrer" target="_blank" class="govuk-link"> Darllenwch ac adolygwch y penderfyniad</a> yn ofalus.',
        'DEFENDANT', 'Click');
