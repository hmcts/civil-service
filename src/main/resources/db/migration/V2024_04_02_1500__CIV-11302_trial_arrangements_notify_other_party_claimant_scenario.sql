/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Required.Defendant"}',
        '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant',
        'The other side has confirmed their trial arrangements', 'Mae''r parti arall wedi cadarnhau eu trefniadau treial',
        '<p class="govuk-body">You can <a href="{VIEW_ORDERS_AND_NOTICES_REDIRECT}" class="govuk-link">view the arrangements that they''ve confirmed</a>.</p>',
        '<p class="govuk-body">Gallwch <a href="{VIEW_ORDERS_AND_NOTICES_REDIRECT}" class="govuk-link">weld y trefniadau y maent wedi''u cadarnhau</a>.</p>',
        'CLAIMANT','Session');

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant', '{7, 7}', 'DEFENDANT', 11);
