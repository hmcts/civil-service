INSERT INTO dbs.scenario (id, name, notifications_to_delete, notifications_to_create, created_at)
VALUES (9999, 'scenario.hearing.fee.payment.required', '{"notification.claim.hearing.readiness.requested"}',
        '{"notification.hearing.fee.payment.required" : ["hearingFeePayByTime", "hearingFeePayByDate"]}',
        '2021-05-09T20:15:45.345875+01:00');
INSERT INTO dbs.scenario (id, name, notifications_to_delete, notifications_to_create, created_at)
VALUES (9998, 'scenario.claimIssue.response.await', '{"notification.claimIssue.claimfee.required"}',
        '{"notification.claimIssue.response.await" : []}',
        '2021-05-09T20:15:45.345875+01:00');

INSERT INTO dbs.dashboard_notifications_Templates (id, template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live, created_at)
VALUES (9998, 'notification.claim.hearing.readiness.requested', 'Pay the hearing fee', 'Pay the hearing fee',
        'Pay the hearing fee. <a href=#>Click here</a>', 'Pay the hearing fee. <a href=#>Click here</a>', 'claimant',
        'singout', '2021-05-09T20:15:45.345875+01:00');

INSERT INTO dbs.dashboard_notifications_Templates (id, template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live, created_at)
VALUES (9999, 'notification.hearing.fee.payment.required', 'Pay the hearing fee', 'Pay the hearing fee',
        '${claimantName} has until <Date> to respond. They can request an extra 28 days if they need it.',
        '${claimantName} has until <Date> to respond. They can request an extra 28 days if they need it.', 'claimant',
        '', '2021-05-09T20:15:45.345875+01:00');

INSERT INTO dbs.dashboard_notifications_Templates (id, template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, time_to_live, created_at)
VALUES (9997, 'notification.claimIssue.response.await', 'Wait for defendant to respond', 'Wait for defendant to respond',
        'Pay the hearing fee. <a href=#>Click here</a>', 'Pay the hearing fee. <a href=#>Click here</a>', 'claimant',
        'singout', '2021-05-09T20:15:45.345875+01:00');

INSERT INTO dbs.task_item_template (id, task_name_en, hint_text_en, category_en, task_name_cy, hint_text_cy, category_cy,
                                    template_name, scenario_name, task_status_sequence, role,task_order ,created_at)
values (9999, '<a href=#>Pay the hearing fee</a>', 'pay by ${hearingFeePayByTime} on ${hearingFeePayByDate}. you have (noOfDays) to pay.'
       , 'Hearing' ,'<a href=#>Pay the hearing fee</a>', 'pay by ${hearingFeePayByTime} on ${hearingFeePayByDate}. you have (noOfDays) to pay.'
        ,'Hearing', 'Hearing.View','scenario.hearing.fee.payment.required', '{1,2}', 'claimant', 10, '2024-02-09T20:15:45.345875+01:00');
