/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ProofofDebtPayment.Application.Claimant',
        '{}','{"Notice.AAA6.ProofofDebtPayment.Application.Claimant": ["coscFullPaymentDateEn", "coscFullPaymentDateCy",  "coscNotificationDateEn", "coscNotificationDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ProofofDebtPayment.Application.Claimant',
        'Confirm if a judgment debt has been paid',
        'Cadarnhau a yw dyled dyfarniad wedi''i thalu',
        '<p class="govuk-body">The defendant who owed you money has told us that they paid in full on ${coscFullPaymentDateEn}. You should <a href="{CONFIRM_YOU_HAVE_BEEN_PAID_URL}" class="govuk-link">confirm if you''ve been paid</a>.<br><br>If you have not been paid, you will need to write to the court where the judgment was issued within a month from ${coscNotificationDateEn} so the application can be reviewed by a district judge. You can find the name of the court on the top right of the judgment. You can then <u>search for the contact details of the court</u> to get the address.</p>',
        '<p class="govuk-body">Mae diffynnydd a oedd arnynt arian ichi wedi dweud wrthym eu bod wedi talu’n llawn ar ${coscFullPaymentDateCy}. Dylech <a href="{CONFIRM_YOU_HAVE_BEEN_PAID_URL}" class="govuk-link">gadarnhau os ydych wedi cael eich talu</a>.<br><br>Os nad ydych wedi cael eich talu, bydd angen i chi ysgrifennu at y llys lle cyhoeddwyd y dyfarniad o fewn mis i ${coscNotificationDateCy} fel y gall barnwr rhanbarth adolygu’r cais. Gallwch ddod o hyd i enw’r llys ar ochr dde uchaf y dyfarniad. Yna gallwch <u>chwilio am fanylion cyswllt y llys</u> i gael y cyfeiriad.</p>',
        'CLAIMANT');
