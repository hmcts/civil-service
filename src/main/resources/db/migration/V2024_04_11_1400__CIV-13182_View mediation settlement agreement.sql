/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class="govuk-link">View mediation settlement agreement</a>',
        'Mediation',
        '<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class="govuk-link">View mediation settlement agreement</a>',
        'Mediation', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationSuccessful.CARM.Claimant', '{3, 3}', 'CLAIMANT', 5);


INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class="govuk-link">View mediation settlement agreement</a>',
        'Mediation',
        '<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class="govuk-link">View mediation settlement agreement</a>',
        'Mediation', 'View.Mediation.Settlement.Agreement', 'Scenario.AAA6.MediationSuccessful.CARM.Defendant', '{3, 3}', 'DEFENDANT', 5);


