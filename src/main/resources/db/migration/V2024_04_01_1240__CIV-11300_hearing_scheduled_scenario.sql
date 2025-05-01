/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Hearing.Scheduled.Claimant',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.Claimant"}',
        '{"Notice.AAA6.CP.Hearing.Scheduled.Claimant" : ["hearingDateEn", "hearingDateCy", "hearingCourtEn", "hearingCourtCy"]}'),
       ('Scenario.AAA6.CP.Hearing.Scheduled.Defendant',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant"}',
        '{"Notice.AAA6.CP.Hearing.Scheduled.Defendant" : ["hearingDateEn", "hearingDateCy", "hearingCourtEn", "hearingCourtCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.Hearing.Scheduled.Claimant', 'A hearing has been scheduled', 'Mae gwrandawiad wedi''i drefnu',
        '<p class="govuk-body">Your hearing has been scheduled for ${hearingDateEn} at ${hearingCourtEn}. Please keep your contact details and anyone you wish to rely on in court up to date. You can update contact details by telephoning the court at 0300 123 7050.</p><p class="govuk-body"><a href="{VIEW_HEARING_NOTICE_CLICK}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the hearing notice</a></p>',
        '<p class="govuk-body">Mae eich gwrandawiad wedi''i drefnu ar gyfer ${hearingDateCy} yn ${hearingCourtCy}. Cadwch eich manylion cyswllt chi a manylion cyswllt unrhyw un yr hoffech ddibynnu arnynt yn y llys yn gyfredol. Gallwch ddiweddaru manylion cyswllt drwy ffonio''r llys ar 0300 303 5174.</p><p class="govuk-body"><a href="{VIEW_HEARING_NOTICE_CLICK}" rel="noopener noreferrer" target="_blank" class="govuk-link">Gweld yr hysbysiad o wrandawiad</a></p>',
        'CLAIMANT', 'Session'),
       ('Notice.AAA6.CP.Hearing.Scheduled.Defendant', 'A hearing has been scheduled', 'Mae gwrandawiad wedi''i drefnu',
        '<p class="govuk-body">Your hearing has been scheduled for ${hearingDateEn} at ${hearingCourtEn}. Please keep your contact details and anyone you wish to rely on in court up to date. You can update contact details by telephoning the court at 0300 123 7050.</p><p class="govuk-body"><a href="{VIEW_HEARING_NOTICE_CLICK}" rel="noopener noreferrer" target="_blank" class="govuk-link">View the hearing notice</a></p>',
        '<p class="govuk-body">Mae eich gwrandawiad wedi''i drefnu ar gyfer ${hearingDateCy} yn ${hearingCourtCy}. Cadwch eich manylion cyswllt chi a manylion cyswllt unrhyw un yr hoffech ddibynnu arnynt yn y llys yn gyfredol. Gallwch ddiweddaru manylion cyswllt drwy ffonio''r llys ar 0300 303 5174.</p><p class="govuk-body"><a href="{VIEW_HEARING_NOTICE_CLICK}" rel="noopener noreferrer" target="_blank" class="govuk-link">Gweld yr hysbysiad o wrandawiad</a></p>',
        'DEFENDANT', 'Session');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_HEARINGS}  rel="noopener noreferrer" class="govuk-link">View the hearing</a>', 'Hearing',
        '<a href={VIEW_HEARINGS}  rel="noopener noreferrer" class="govuk-link">Gweld y gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.View', 'Scenario.AAA6.CP.Hearing.Scheduled.Claimant', '{3, 3}', 'CLAIMANT', 8),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>',
        'Gorchmynion a rhybuddion gan y llys', 'Order.View', 'Scenario.AAA6.CP.Hearing.Scheduled.Claimant', '{3, 3}', 'CLAIMANT', 14),
       ('<a href={VIEW_HEARINGS}  rel="noopener noreferrer" class="govuk-link">View the hearing</a>', 'Hearing',
        '<a href={VIEW_HEARINGS}  rel="noopener noreferrer" class="govuk-link">Gweld y gwrandawiad</a>',
        'Gwrandawiad', 'Hearing.View', 'Scenario.AAA6.CP.Hearing.Scheduled.Defendant', '{3, 3}', 'DEFENDANT', 8),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>',
        'Gorchmynion a rhybuddion gan y llys', 'Order.View', 'Scenario.AAA6.CP.Hearing.Scheduled.Defendant', '{3, 3}', 'DEFENDANT', 13);
