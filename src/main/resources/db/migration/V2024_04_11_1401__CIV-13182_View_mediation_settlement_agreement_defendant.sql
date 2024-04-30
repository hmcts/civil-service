/**
 * Add task list items defendant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class="govuk-link">View mediation settlement agreement</a>',
        'Mediation',
        '<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class="govuk-link">View mediation settlement agreement</a>',
        'Mediation', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{3, 3}', 'DEFENDANT', 5),

('<a>Upload mediation documents</a>',
  'Mediation',
  '<a>Upload mediation documents</a>',
  'Mediation', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 6),

('<a>View mediation documents</a>',
  'Mediation',
  '<a>View mediation documents</a>',
  'Mediation', 'View.Mediation.Documents', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 7),

('<a>View hearings</a>',
  'Hearings',
  '<a>View hearings</a>',
  'Hearings', 'Hearing.View', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 8),

('<a>Upload hearing documents</a>',
  'Hearings',
  '<a>Upload hearing documents</a>',
  'Hearings', 'Hearing.Document.Upload', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 9),

('<a>Pay the hearing fee</a>',
  'Hearings',
  '<a>Pay the hearing fee</a>',
  'Hearings', 'Hearing.Fee.Pay', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 11),

('<a>View the bundle</a>',
  'Hearings',
  '<a>View the bundle</a>',
  'Hearings', 'Hearing.Bundle.View', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 12),

('<a>View the judgment</a>',
  'Judgments from the court',
  '<a>View the judgment</a>',
  'Judgments from the court', 'Judgment.View', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 13),

('<a>View applications</a>',
  'Applications',
  '<a>View applications</a>',
  'Applications', 'Application.View', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{2, 2}', 'DEFENDANT', 14);
