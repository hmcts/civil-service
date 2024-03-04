/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.Notice.ClaimIssue.HWF.Rejected', '{"Notice.AAA7.ClaimIssue.HWF.Requested","Notice.AAA7.ClaimIssue.HWF.Requested","Notice.AAA7.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA7.ClaimIssue.HWF.Rejected" : ["claimFeeAmount","paymentDueDate"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA7.ClaimIssue.HWF.Rejected', 'Your help with fees application has been rejected',
        'Your help with fees application has been rejected',
        'We''ve rejected your application for help with the claim fee. See the email for further details. You''ll need to pay the full fee of ${claimFeeAmount} by ${paymentDueDate}. You can pay by phone by calling 0300 123 7050',
        'We''ve rejected your application for help with the claim fee. See the email for further details. You''ll need to pay the full fee of ${claimFeeAmount} by ${paymentDueDate}. You can pay by phone by calling 0300 123 7050',
        'CLAIMANT');

