/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a>View mediation settlement agreement</a>',
        'Mediation',
        '<a>Gweld cytundeb setlo o ran cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationUnsuccessful.NOTClaimant1NonContactable.CARM.Claimant', '{2, 2}', 'CLAIMANT', 5),

       ('<a>Upload mediation documents</a>',
        'Mediation',
        '<a>Uwchlwytho dogfennau cyfryngu</a>',
        'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.NOTClaimant1NonContactable.CARM.Claimant', '{2, 2}', 'CLAIMANT', 6),

       ('<a>View mediation documents</a>',
        'Mediation',
        '<a>Gweld dogfennau cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.NOTClaimant1NonContactable.CARM.Claimant', '{2, 2}', 'CLAIMANT', 7);


/**
 * Add task list items defendant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a>View mediation settlement agreement</a>',
        'Mediation',
        '<a>Gweld cytundeb setlo o ran cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationUnsuccessful.NOTDefendant1NonContactable.CARM.Defendant', '{2, 2}', 'DEFENDANT', 5),

       ('<a>Upload mediation documents</a>',
        'Mediation',
        '<a>Uwchlwytho dogfennau cyfryngu</a>',
        'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.NOTDefendant1NonContactable.CARM.Defendant', '{2, 2}', 'DEFENDANT', 6),

       ('<a>View mediation documents</a>',
        'Mediation',
        '<a>Gweld dogfennau cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.NOTDefendant1NonContactable.CARM.Defendant', '{2, 2}', 'DEFENDANT', 7);
