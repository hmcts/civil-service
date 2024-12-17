/**
 * Delete scenarios
 */
DELETE FROM dbs.scenario WHERE(name = 'Scenario.AAA6.CP.Bundle.Ready.TrialReady.Claimant')
                            OR(name = 'Scenario.AAA6.CP.Bundle.Ready.TrialReady.Defendant');

/**
 * Add scenarios
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.TrialReady.Check.Claimant', '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}', '{}'),
       ('Scenario.AAA6.CP.TrialReady.Check.Defendant', '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Defendant", "Notice.AAA6.CP.Trial.Arrangements.Required.Defendant"}', '{}');

/**
 * Delete task items
 */
DELETE FROM dbs.task_item_template
WHERE (template_name = 'Hearing.Arrangements.Add' AND scenario_name = 'Scenario.AAA6.CP.Bundle.Ready.Claimant' AND task_order = 13)
   OR (template_name = 'Hearing.Arrangements.Add' AND scenario_name = 'Scenario.AAA6.CP.Bundle.Ready.Defendant' AND task_order = 12)
   OR (template_name = 'Hearing.Bundle.View' AND scenario_name = 'Scenario.AAA6.CP.Bundle.Ready.TrialReady.Claimant' AND task_order = 12)
   OR (template_name = 'Hearing.Bundle.View' AND scenario_name = 'Scenario.AAA6.CP.Bundle.Ready.TrialReady.Defendant' AND task_order = 11);

/**
 * Add task item template
 */

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order, hint_text_en, hint_text_cy)
values  ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
          'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.TrialReady.Check.Claimant', '{2, 2}', 'CLAIMANT', 12),
        ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
          'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.TrialReady.Check.Defendant', '{2, 2}', 'DEFENDANT', 11);
