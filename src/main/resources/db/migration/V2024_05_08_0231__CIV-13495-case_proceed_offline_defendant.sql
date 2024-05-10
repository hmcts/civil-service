/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CaseProceedsInCaseman.Defendant', '{}', '{"Notice.AAA6.CaseProceedsInCaseman.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live)
VALUES ('Notice.AAA6.CaseProceedsInCaseman.Defendant', 'Your online account will no longer be updated', 'Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach',
        '<p class="govuk-body">Your online account will no longer be updated. If there are any further updates to your case these will be by post.</p>',
        '<p class="govuk-body">Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach. Os oes unrhyw ddiweddariadau pellach i’ch achos, bydd y rhain yn cael eu hanfon atoch drwy''r post.</p>',
        'DEFENDANT', 'Session');
