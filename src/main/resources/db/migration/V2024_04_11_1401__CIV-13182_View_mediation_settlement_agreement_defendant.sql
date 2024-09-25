/**
 * Add task list items defendant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class="govuk-link">View mediation settlement agreement</a>',
        'Mediation',
        '<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class="govuk-link">Gweld cytundeb setlo o ran cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{3, 3}', 'DEFENDANT', 5),

('<a>Upload mediation documents</a>',
  'Mediation',
  '<a>Uwchlwytho dogfennau cyfryngu</a>',
  'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 6),

('<a>View mediation documents</a>',
  'Mediation',
  '<a>Gweld dogfennau cyfryngu</a>',
  'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 7),

('<a>View hearings</a>',
  'Hearing',
  '<a>Gweld y gwrandawiad</a>',
  'Gwrandawiad', 'Hearing.View', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 8),

('<a>Upload hearing documents</a>',
  'Hearing',
  '<a>Llwytho dogfennau''r gwrandawiad</a>',
  'Gwrandawiad', 'Hearing.Document.Upload', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 9),

('<a>Add the trial arrangements</a>',
  'Hearing',
  '<a>Ychwanegu trefniadau''r treial</a>',
  'Gwrandawiad', 'Hearing.Arrangements.Add', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 10),

('<a>View the bundle</a>',
  'Hearing',
  '<a>Gweld y bwndel</a>',
  'Gwrandawiad', 'Hearing.Bundle.View', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 12),

('<a>View the judgment</a>',
  'Judgment from the court',
  '<a>Gweld y Dyfarniad</a>',
  'Dyfarniadau gan y llys', 'Judgment.View', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 13),

('<a>Confirm you''ve paid a judgment (CCJ) debt</a>',
'Judgment from the court',
'<a>Cadarnhewch eich bod wedi talu dyled dyfarniad (CCJ)</a>',
'Dyfarniadau gan y llys', 'Judgment.Cosc', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 14),

('<a>View applications</a>',
  'Applications',
  '<a>Gweld y cais i gyd</a>',
  'Ceisiadau', 'Application.View', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 15);
