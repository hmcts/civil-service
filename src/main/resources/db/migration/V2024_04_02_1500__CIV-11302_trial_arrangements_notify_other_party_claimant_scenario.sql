/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Required.BothParties"}',
        '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant',
        'The other side has confirmed their trial arrangements', 'The other side has confirmed their trial arrangements',
        '<p class="govuk-body">You can <a href="{VIEW_ORDERS_AND_NOTICES_REDIRECT}" class="govuk-link">view the arrangements that they''ve confirmed.</a></p>',
        '<p class="govuk-body">You can <a href="{VIEW_ORDERS_AND_NOTICES_REDIRECT}" class="govuk-link">view the arrangements that they''ve confirmed.</a></p>',
        'CLAIMANT','Session');
