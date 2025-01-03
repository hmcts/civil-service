/**
  Update scenarios
 */
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.Bundle.Ready.Claimant"}'
where name = 'Scenario.AAA6.CP.Bundle.Updated.TrialReady.Claimant';
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.Bundle.Ready.Defendant"}'
where name = 'Scenario.AAA6.CP.Bundle.Updated.TrialReady.Defendant';
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.Bundle.Ready.Claimant"}'
where name = 'Scenario.AAA6.CP.Bundle.Updated.Claimant';
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.Bundle.Ready.Defendant"}'
where name = 'Scenario.AAA6.CP.Bundle.Updated.Defendant';
update dbs.scenario set notifications_to_delete = '{}'
where name = 'Scenario.AAA6.CP.Bundle.Ready.TrialReady.Claimant';
update dbs.scenario set notifications_to_delete = '{}'
where name = 'Scenario.AAA6.CP.Bundle.Ready.TrialReady.Defendant';
update dbs.scenario set notifications_to_delete = '{}'
where name = 'Scenario.AAA6.CP.Bundle.Ready.Claimant';
update dbs.scenario set notifications_to_delete = '{}'
where name = 'Scenario.AAA6.CP.Bundle.Ready.Defendant';

/**
 * Add scenarios
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.TrialReady.Check.Claimant', '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}', '{}'),
       ('Scenario.AAA6.CP.TrialReady.Check.Defendant', '{"Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Defendant", "Notice.AAA6.CP.Trial.Arrangements.Required.Defendant"}', '{}');

/**
 * Add task item template
 */

INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values  ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
          'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.TrialReady.Check.Claimant', '{2, 2}', 'CLAIMANT', 12),
        ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
          'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.CP.TrialReady.Check.Defendant', '{2, 2}', 'DEFENDANT', 11);
