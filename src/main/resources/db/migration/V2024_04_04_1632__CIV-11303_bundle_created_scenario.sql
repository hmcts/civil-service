/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Bundle.Ready.Claimant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Defendant"}',
        '{"Notice.AAA6.CP.Bundle.Ready.Claimant" : []}'),
       ('Scenario.AAA6.CP.Bundle.Ready.Defendant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant"}',
        '{"Notice.AAA6.CP.Bundle.Ready.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.Bundle.Ready.Claimant', 'The bundle is ready to view', 'The bundle is ready to view',
        '<p class="govuk-body">The bundle contains all the documents that will be referred to at the hearing. <a href="{VIEW_BUNDLE}" class="govuk-link">Review the bundle</a> to ensure that the information is accurate.</p>',
        '<p class="govuk-body">The bundle contains all the documents that will be referred to at the hearing. <a href="{VIEW_BUNDLE}" class="govuk-link">Review the bundle</a> to ensure that the information is accurate.</p>',
        'CLAIMANT'),
       ('Notice.AAA6.CP.Bundle.Ready.Defendant', 'The bundle is ready to view', 'The bundle is ready to view',
        '<p class="govuk-body">The bundle contains all the documents that will be referred to at the hearing. <a href="{VIEW_BUNDLE}" class="govuk-link">Review the bundle</a> to ensure that the information is accurate.</p>',
        '<p class="govuk-body">The bundle contains all the documents that will be referred to at the hearing. <a href="{VIEW_BUNDLE}" class="govuk-link">Review the bundle</a> to ensure that the information is accurate.</p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearings',
        '<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearings', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Ready.Claimant', '{3, 3}', 'CLAIMANT', 9),
       ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearings',
        '<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearings', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Ready.Defendant', '{3, 3}', 'DEFENDANT', 8);
