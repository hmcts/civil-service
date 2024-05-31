/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationUnsuccessful.WhenDefendantNotContactable.CARM.Claimant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Claimant", "Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Claimant"}',
        '{"Notice.AAA6.MediationUnsuccessful.NOTClaimant1NonContactable.CARM.Claimant":[]}');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a>View mediation settlement agreement</a>',
        'Mediation',
        '<a>Gweld cytundeb setlo o ran cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationUnsuccessful.WhenDefendantNotContactable.CARM.Claimant', '{2, 2}', 'CLAIMANT', 5),

       ('<a>Upload mediation documents</a>',
        'Mediation',
        '<a>Uwchlwytho dogfennau cyfryngu</a>',
        'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.WhenDefendantNotContactable.CARM.Claimant', '{2, 2}', 'CLAIMANT', 6),

       ('<a>View mediation documents</a>',
        'Mediation',
        '<a>Gweld dogfennau cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.WhenDefendantNotContactable.CARM.Claimant', '{1, 1}', 'CLAIMANT', 7);


/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationUnsuccessful.WhenClaimantNotContactable.CARM.Defendant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Claimant", "Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant"}',
        '{"Notice.AAA6.MediationUnsuccessful.NOTDefendant1NonContactable.CARM.Defendant":[]}');

/**
 * Add task list items defendant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a>View mediation settlement agreement</a>',
        'Mediation',
        '<a>Gweld cytundeb setlo o ran cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationUnsuccessful.WhenClaimantNotContactable.CARM.Defendant', '{2, 2}', 'DEFENDANT', 5),

       ('<a>Upload mediation documents</a>',
        'Mediation',
        '<a>Uwchlwytho dogfennau cyfryngu</a>',
        'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.WhenClaimantNotContactable.CARM.Defendant', '{2, 2}', 'DEFENDANT', 6),

       ('<a>View mediation documents</a>',
        'Mediation',
        '<a>Gweld dogfennau cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.WhenClaimantNotContactable.CARM.Defendant', '{1, 1}', 'DEFENDANT', 7);
