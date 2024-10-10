/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES
  ('Scenario.AAA6.CP.Bundle.Updated.TrialReady.Claimant',
   '{"Notice.AAA6.CP.Bundle.Ready.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}',
   '{"Notice.AAA6.CP.Bundle.Updated.Claimant": ["bundleRestitchedDateEn", "bundleRestitchedDateCy"]}'),
  ('Scenario.AAA6.CP.Bundle.Updated.TrialReady.Defendant',
   '{"Notice.AAA6.CP.Bundle.Ready.Defendant", "Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Defendant", "Notice.AAA6.CP.Trial.Arrangements.Required.Defendant"}',
   '{"Notice.AAA6.CP.Bundle.Updated.Defendant": ["bundleRestitchedDateEn", "bundleRestitchedDateCy"]}');

/**
 * Add task item template
 */

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearing',
        '<a href={VIEW_BUNDLE} class="govuk-link">Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Updated.Claimant', '{3, 3}', 'CLAIMANT', 13),
       ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearing',
        '<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Updated.Defendant', '{3, 3}', 'DEFENDANT', 12),
       ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearing',
        '<a href={VIEW_BUNDLE} class="govuk-link">Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Updated.TrialReady.Claimant', '{3, 3}', 'CLAIMANT', 13),
       ('<a href={VIEW_BUNDLE} class="govuk-link">View the bundle</a>',
        'Hearing',
        '<a href={VIEW_BUNDLE} class="govuk-link">Gweld y bwndel</a>',
        'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.CP.Bundle.Updated.TrialReady.Defendant', '{3, 3}', 'DEFENDANT', 12),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Bundle.Updated.Claimant', '{2, 2}', 'CLAIMANT', 12),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.Bundle.Updated.Defendant', '{2, 2}', 'DEFENDANT', 11);
