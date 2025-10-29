INSERT INTO dbs.scenario (id, name, notifications_to_delete, notifications_to_create, created_at)
VALUES (9999, 'scenario.hearing.fee.payment.required', '{"notification.claim.hearing.readiness.requested"}',
        '{"notification.hearing.fee.payment.required" : ["hearingFeePayByTime", "hearingFeePayByDate"]}',
        '2021-05-09T20:15:45.345875+01:00');

INSERT INTO dbs.task_item_template (id, task_name_en, hint_text_en, category_en, task_name_cy, hint_text_cy, category_cy,
                                    template_name, scenario_name, task_status_sequence, role,task_order ,created_at)
values (9999, '<a href=#>Pay the hearing fee</a>', 'pay by ${hearingFeePayByTime} on ${hearingFeePayByDate}. you have (noOfDays) to pay.'
       , 'Hearing' ,'<a href=#>Pay the hearing fee</a>', 'pay by ${hearingFeePayByTime} on ${hearingFeePayByDate}. you have (noOfDays) to pay.'
       ,'Hearing', 'Hearing.View','scenario.hearing.fee.payment.required', '{1,2}', 'claimant', 10, '2024-02-09T20:15:45.345875+01:00');

INSERT INTO dbs.scenario (id, name, notifications_to_delete, notifications_to_create, created_at)
VALUES (9997, 'Scenario.AAA6.ClaimIssue.ClaimFee.Required.Test', '{"Notice.AAA6.ClaimIssue.ClaimSubmit.Required.Test"}',
        '{"Notice.AAA6.ClaimIssue.ClaimFee.Required.Test" : ["claimFee"]}',
        '2021-05-09T20:15:45.345875+01:00');
