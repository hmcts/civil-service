/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Bundle.Ready.Claimant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}',
        '{"Notice.AAA6.CP.Bundle.Ready.Claimant" : []}'),
       ('Scenario.AAA6.CP.Bundle.Ready.Defendant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Defendant", "Notice.AAA6.CP.Trial.Arrangements.Required.Defendant"}',
        '{"Notice.AAA6.CP.Bundle.Ready.Defendant" : []}'),
       ('Scenario.AAA6.CP.Bundle.Ready.TrialReady.Claimant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}',
        '{"Notice.AAA6.CP.Bundle.Ready.Claimant" : []}'),
       ('Scenario.AAA6.CP.Bundle.Ready.TrialReady.Defendant',
        '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Defendant", "Notice.AAA6.CP.Trial.Arrangements.Required.Defendant"}',
        '{"Notice.AAA6.CP.Bundle.Ready.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy,
                                                   notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.Bundle.Ready.Claimant', 'The bundle is ready to view', 'Mae''r bwndel yn barod i''w weld',
        '<p class="govuk-body">The bundle contains all the documents that will be referred to at the hearing. <a href="{VIEW_BUNDLE_REDIRECT}" class="govuk-link">Review the bundle</a> to ensure that the information is accurate.</p>',
        '<p class="govuk-body">Mae''r bwndel yn cynnwys yr holl ddogfennau y cyfeirir atynt yn y gwrandawiad. <a href="{VIEW_BUNDLE_REDIRECT}" class="govuk-link">Adolygwch y bwndel</a> i sicrhau bod yr wybodaeth yn gywir.</p>',
        'CLAIMANT', 'Session'),
       ('Notice.AAA6.CP.Bundle.Ready.Defendant', 'The bundle is ready to view', 'Mae''r bwndel yn barod i''w weld',
        '<p class="govuk-body">The bundle contains all the documents that will be referred to at the hearing. <a href="{VIEW_BUNDLE_REDIRECT}" class="govuk-link">Review the bundle</a> to ensure that the information is accurate.</p>',
        '<p class="govuk-body">Mae''r bwndel yn cynnwys yr holl ddogfennau y cyfeirir atynt yn y gwrandawiad. <a href="{VIEW_BUNDLE_REDIRECT}" class="govuk-link">Adolygwch y bwndel</a> i sicrhau bod yr wybodaeth yn gywir.</p>',
        'DEFENDANT', 'Session');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearing',
        '<a href={VIEW_BUNDLE} class="govuk-link">Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Ready.Claimant', '{3, 3}', 'CLAIMANT', 13),
       ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearing',
        '<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Ready.Defendant', '{3, 3}', 'DEFENDANT', 12),
       ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearing',
        '<a href={VIEW_BUNDLE} class="govuk-link">Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Ready.TrialReady.Claimant', '{3, 3}', 'CLAIMANT', 13),
       ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearing',
        '<a href={VIEW_BUNDLE} class="govuk-link">Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Ready.TrialReady.Defendant', '{3, 3}', 'DEFENDANT', 12),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Bundle.Ready.Claimant', '{2, 2}', 'CLAIMANT', 12),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Bundle.Ready.Defendant', '{2, 2}', 'DEFENDANT', 11);
