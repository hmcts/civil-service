INSERT INTO dbs.dashboard_notifications_Templates (id, template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role, notifications_To_Be_Deleted, time_to_live, created_At)
VALUES (1, 'notice-hearing-fee-payment-required', 'Pay the hearing fee', 'Pay the hearing fee',
        'Pay the hearing fee. <a href=#>Click here</a>', 'Pay the hearing fee. <a href=#>Click here</a>', 'claimant',
        '{}', 'singout', '2021-05-09T20:15:45.345875+01:00');

INSERT INTO dbs.task_item_template (id, task_name_en, hint_text_en, category_en, task_name_cy, hint_text_cy, category_cy,
                                    name, task_status_sequence, role,task_order ,created_at)
values (1, '<a href=#>Pay the hearing fee</a>', 'pay by ${hearingFeePayByTime} on ${hearingFeePayByDate}. you have (noOfDays) to pay.'
       , 'Hearing' ,'<a href=#>Pay the hearing fee</a>', 'pay by ${hearingFeePayByTime} on ${hearingFeePayByDate}. you have (noOfDays) to pay.'
        ,'Hearing', 'notice-hearing-fee-payment-required', '{1,2}', 'claimant', 10, '2024-02-09T20:15:45.345875+01:00');
