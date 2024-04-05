/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Hearing.Scheduled.BothParties',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.Claimant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant", "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant"}',
        '{"Notice.AAA6.CP.Hearing.Scheduled.Defendant" : ["hearingDateEn", "hearingDateCy", "hearingCourtEn", "hearingCourtCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.Hearing.Scheduled.BothParties', 'A hearing has been scheduled', 'A hearing has been scheduled',
        '<p class="govuk-body">Your hearing has been scheduled for ${hearingDateEn} at ${hearingCourtEn}. Please keep your contact details and anyone you wish to rely on in court up to date. You can update contact details by telephoning the court at 0300 123 7050. <a href="{VIEW_HEARING_NOTICE}" class="govuk-link">View the hearing notice</a>.</p>',
        '<p class="govuk-body">Your hearing has been scheduled for ${hearingDateCy} at ${hearingCourtCy}. Please keep your contact details and anyone you wish to rely on in court up to date. You can update contact details by telephoning the court at 0300 123 7050. <a href="{VIEW_HEARING_NOTICE}" class="govuk-link">View the hearing notice</a>.</p>',
        'CLAIMANT'),
       ('Notice.AAA6.CP.Hearing.Scheduled.BothParties', 'A hearing has been scheduled', 'A hearing has been scheduled',
        '<p class="govuk-body">Your hearing has been scheduled for ${hearingDateEn} at ${hearingCourtEn}. Please keep your contact details and anyone you wish to rely on in court up to date. You can update contact details by telephoning the court at 0300 123 7050. <a href="{VIEW_HEARING_NOTICE}" class="govuk-link">View the hearing notice</a>.</p>',
        '<p class="govuk-body">Your hearing has been scheduled for ${hearingDateCy} at ${hearingCourtCy}. Please keep your contact details and anyone you wish to rely on in court up to date. You can update contact details by telephoning the court at 0300 123 7050. <a href="{VIEW_HEARING_NOTICE}" class="govuk-link">View the hearing notice</a>.</p>',
        'DEFENDANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_HEARINGS}  rel="noopener noreferrer" class="govuk-link">View hearings</a>', 'Hearings',
        '<a href={VIEW_HEARINGS}  rel="noopener noreferrer" class="govuk-link">View hearings</a>',
        'Hearings', 'Hearing.View', 'Scenario.AAA6.CP.Hearing.Scheduled.BothParties', '{3, 3}', 'CLAIMANT', 5),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA6.CP.Hearing.Scheduled.BothParties', '{3, 3}', 'CLAIMANT', 10),
       ('<a href={VIEW_HEARINGS}  rel="noopener noreferrer" class="govuk-link">View hearings</a>', 'Hearings',
        '<a href={VIEW_HEARINGS}  rel="noopener noreferrer" class="govuk-link">View hearings</a>',
        'Hearings', 'Hearing.View', 'Scenario.AAA6.CP.Hearing.Scheduled.BothParties', '{3, 3}', 'DEFENDANT', 5),
       ('<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES}  rel="noopener noreferrer" class="govuk-link">View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA6.CP.Hearing.Scheduled.BothParties', '{3, 3}', 'DEFENDANT', 10);
