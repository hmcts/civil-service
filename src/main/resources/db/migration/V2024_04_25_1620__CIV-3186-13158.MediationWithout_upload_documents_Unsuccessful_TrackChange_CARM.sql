/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Claimant',
        '{"Notice.AAA6.MediationUnsuccessful.Claimant1NonAttendance.CARM.Claimant"}',
        '{"Notice.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Claimant": []}');

/**
 * Add notification template for claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Claimant',
        'You are no longer required to submit documents relating to mediation non-attendance',
        'Nid yw''n ofynnol bellach i chi gyflwyno dogfennau sy''n ymwneud â pheidio â mynychu cyfryngu',
        '<p class="govuk-body">A judge has reviewed your case and you are no longer required to submit documents relating to non-attendance of a mediation appointment. There will be no penalties issued for your non-attendance.</p>',
        '<p class="govuk-body">Mae barnwr wedi adolygu eich achos ac nid yw''n ofynnol i chi gyflwyno dogfennau bellach yn ymwneud â pheidio â mynychu apwyntiad cyfryngu. Ni fydd unrhyw gosbau yn cael eu gosod am eich diffyg presenoldeb.</p>',
        'CLAIMANT');

/**
 * Add task list items claimant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a>Upload mediation documents</a>',
        'Mediation',
        '<a>Uwchlwytho dogfennau cyfryngu</a>',
        'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Claimant', '{2, 2}', 'CLAIMANT', 6),
       ('<a>View mediation documents</a>',
        'Mediation',
        '<a>Gweld dogfennau cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Claimant', '{2, 2}', 'CLAIMANT', 7);

/**
 * Add scenario for defendant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant',
        '{"Notice.AAA6.MediationUnsuccessful.Defendant1NonAttendance.CARM.Defendant"}',
        '{"Notice.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant": []}');

/**
 * Add notification template from defendant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant',
        'You are no longer required to submit documents relating to mediation non-attendance',
        'Nid yw''n ofynnol bellach i chi gyflwyno dogfennau sy''n ymwneud â pheidio â mynychu cyfryngu',
        '<p class="govuk-body">A judge has reviewed your case and you are no longer required to submit documents relating to non-attendance of a mediation appointment. There will be no penalties issued for your non-attendance.</p>',
        '<p class="govuk-body">Mae barnwr wedi adolygu eich achos ac nid yw''n ofynnol i chi gyflwyno dogfennau bellach yn ymwneud â pheidio â mynychu apwyntiad cyfryngu. Ni fydd unrhyw gosbau yn cael eu gosod am eich diffyg presenoldeb.</p>',
        'DEFENDANT');

/**
 * Add task list items defendant
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a>Upload mediation documents</a>',
        'Mediation',
        '<a>Uwchlwytho dogfennau cyfryngu</a>',
        'Cyfryngu', 'Upload.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant', '{2, 2}', 'DEFENDANT', 6),
       ('<a>View mediation documents</a>',
        'Mediation',
        '<a>Gweld dogfennau cyfryngu</a>',
        'Cyfryngu', 'View.Mediation.Documents', 'Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant', '{2, 2}', 'DEFENDANT', 7);


