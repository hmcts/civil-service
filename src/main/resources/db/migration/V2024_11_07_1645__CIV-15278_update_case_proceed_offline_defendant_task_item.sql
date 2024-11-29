
/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Update.CaseProceedsInCaseman.Defendant', '{}', '{"Notice.AAA6.CaseProceedsInCaseman.Defendant" : []}'),
       ('Scenario.AAA6.Update.CaseProceedsInCaseman.Defendant.FastTrack', '{}', '{"Notice.AAA6.CaseProceedsInCaseman.Defendant" : []}');


INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role,task_order)
values ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.Update.CaseProceedsInCaseman.Defendant', '{2, 2}', 'DEFENDANT', 10),
       ('<a>Upload hearing documents</a>', 'Hearing' ,'<a>Llwytho dogfennau''r gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.Update.CaseProceedsInCaseman.Defendant.FastTrack', '{2, 2}', 'DEFENDANT', 10),
       ('<a>Add the trial arrangements</a>', 'Hearing' ,'<a>Ychwanegu trefniadau''r treial</a>',
        'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.Update.CaseProceedsInCaseman.Defendant.FastTrack', '{2, 2}', 'DEFENDANT', 11),
       ('<a>Confirm you''ve paid a judgment (CCJ) debt</a>', 'Judgments from the court',
        '<a>Cadarnhewch eich bod wedi talu dyled dyfarniad (CCJ)</a>', 'Dyfarniadau gan y llys',
        'Judgment.Cosc', 'Scenario.AAA6.Update.CaseProceedsInCaseman.Defendant.FastTrack', '{2, 2}', 'DEFENDANT', 15),
       ('<a>Confirm you''ve paid a judgment (CCJ) debt</a>', 'Judgments from the court',
        '<a>Cadarnhewch eich bod wedi talu dyled dyfarniad (CCJ)</a>', 'Dyfarniadau gan y llys',
        'Judgment.Cosc', 'Scenario.AAA6.Update.CaseProceedsInCaseman.Defendant', '{2, 2}', 'DEFENDANT', 15);
