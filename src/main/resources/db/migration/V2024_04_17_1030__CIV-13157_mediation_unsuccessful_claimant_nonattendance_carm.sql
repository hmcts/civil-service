/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationUnsuccessful.Claimant1NonAttendance.CARM.Claimant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Claimant", "Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Claimant"}',
        '{"Notice.AAA6.MediationUnsuccessful.Claimant1NonAttendance.CARM.Claimant":[]}');

/**
 * Add notification template for claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.MediationUnsuccessful.Claimant1NonAttendance.CARM.Claimant',
        'You did not attend mediation',
        'Ni wnaethoch fynychu cyfryngu',
        '<p class="govuk-body">You did not attend your mediation appointment, and the judge may issue a penalty against you. Your case will now be reviewed by the court. <a href="{UPLOAD_MEDIATION_DOCUMENTS}" class="govuk-link">Explain why you did not attend your appointment</a></p>',
        '<p class="govuk-body">Ni wnaethoch fynychu eich apwyntiad cyfryngu, ac efallai y bydd y barnwr yn eich cosbi. Bydd yr achos hwn nawr yn cael ei adolygu gan y llys. <a href="{UPLOAD_MEDIATION_DOCUMENTS}" class="govuk-link">Esboniwch pam na wnaethoch chi fynychu eich apwyntiad</a></p>',
        'CLAIMANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a>View mediation settlement agreement</a>',
        'Mediation',
        '<a>Gweld cytundeb setlo o ran cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationUnsuccessful.Claimant1NonAttendance.CARM.Claimant', '{2, 2}', 'CLAIMANT', 5),

       ('<a href={UPLOAD_MEDIATION_DOCUMENTS} class="govuk-link">Upload mediation documents</a>',
        'Mediation',
        '<a href={UPLOAD_MEDIATION_DOCUMENTS} class="govuk-link">Uwchlwytho dogfennau cyfryngu</a>',
        'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.Claimant1NonAttendance.CARM.Claimant', '{5, 5}', 'CLAIMANT', 6),

       ('<a>View mediation documents</a>',
        'Mediation',
        '<a>Gweld dogfennau cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.Claimant1NonAttendance.CARM.Claimant', '{1, 1}', 'CLAIMANT', 7);

/**
 * Add scenario for defendant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationUnsuccessful.Defendant1NonAttendance.CARM.Defendant',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Defendant", "Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant"}',
        '{"Notice.AAA6.MediationUnsuccessful.Defendant1NonAttendance.CARM.Defendant":[]}');

/**
 * Add notification template for defendant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.MediationUnsuccessful.Defendant1NonAttendance.CARM.Defendant',
        'You did not attend mediation',
        'Ni wnaethoch fynychu cyfryngu',
        '<p class="govuk-body">You did not attend your mediation appointment, and the judge may issue a penalty against you. Your case will now be reviewed by the court. <a href="{UPLOAD_MEDIATION_DOCUMENTS}" class="govuk-link">Explain why you did not attend your appointment</a></p>',
        '<p class="govuk-body">Ni wnaethoch fynychu eich apwyntiad cyfryngu, ac efallai y bydd y barnwr yn eich cosbi. Bydd yr achos hwn nawr yn cael ei adolygu gan y llys. <a href="{UPLOAD_MEDIATION_DOCUMENTS}" class="govuk-link">Esboniwch pam na wnaethoch chi fynychu eich apwyntiad</a></p>',
        'DEFENDANT');

/**
 * Add task list items defendant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)

values ('<a>View mediation settlement agreement</a>',
        'Mediation',
        '<a>Gweld cytundeb setlo o ran cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationUnsuccessful.Defendant1NonAttendance.CARM.Defendant', '{2, 2}', 'DEFENDANT', 5),

       ('<a href={UPLOAD_MEDIATION_DOCUMENTS} class="govuk-link">Upload mediation documents</a>',
        'Mediation',
        '<a href={UPLOAD_MEDIATION_DOCUMENTS} class="govuk-link">Uwchlwytho dogfennau cyfryngu</a>',
        'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.Defendant1NonAttendance.CARM.Defendant', '{5, 5}', 'DEFENDANT', 6),

       ('<a>View mediation documents</a>',
        'Mediation',
        '<a>Gweld dogfennau cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessful.Defendant1NonAttendance.CARM.Defendant', '{1, 1}', 'DEFENDANT', 7);

