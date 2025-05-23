INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.LiPQM.CaseOffline.OpenQueries.Claimant', '{}', '{"Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Claimant" : []}'),
       ('Scenario.AAA6.LiPQM.CaseOffline.OpenQueries.Defendant', '{}', '{"Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Defendant" : []}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy ,notification_role)
VALUES ('Scenario.AAA6.LiPQM.CaseOffline.OpenQueries.Claimant',
        'Your claim is now offline, but the court has not answered a message you sent',
        'Mae eich hawliad all-lein bellach, ond nid yw’r llys wedi ateb neges a anfonwyd gennych',
        '<p class="govuk-body">You may still get an answer to your message on your dashboard, but all other updates on your claim will be by email or post.</p>',
        '<p class="govuk-body">Mae''n bosibl y byddwch yn dal i gael ateb i’ch neges ar eich dangosfwrdd, ond bydd yr holl ddiweddariadau eraill ar eich hawliad yn digwydd trwy e-bost neu’r post.</p>',
        'CLAIMANT'),
        ('Scenario.AAA6.LiPQM.CaseOffline.OpenQueries.Defendant',
        'Your claim is now offline, but the court has not answered a message you sent',
        'Mae eich hawliad all-lein bellach, ond nid yw’r llys wedi ateb neges a anfonwyd gennych',
        '<p class="govuk-body">You may still get an answer to your message on your dashboard, but all other updates on your claim will be by email or post.</p>',
        '<p class="govuk-body">Mae''n bosibl y byddwch yn dal i gael ateb i’ch neges ar eich dangosfwrdd, ond bydd yr holl ddiweddariadau eraill ar eich hawliad yn digwydd trwy e-bost neu’r post.</p>',
        'DEFENDANT');
