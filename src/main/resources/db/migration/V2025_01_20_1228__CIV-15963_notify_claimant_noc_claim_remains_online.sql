/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefendantNoticeOfChange.ClaimRemainsOnline.Claimant',
        '{}',
        '{"Notice.AAA6.DefendantNoticeOfChange.ClaimRemainsOnline.Claimant": ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.DefendantNoticeOfChange.ClaimRemainsOnline.Claimant',
        '${respondent1PartyName} has assigned a legal representative to act on their behalf',
        'Mae ${respondent1PartyName} wedi neilltuo cynrychiolydd cyfreithiol i weithredu ar ei ran',
        '<p class="govuk-body">You will now need to liaise with their legal representation.</p><p class="govuk-body"><a href="{VIEW_INFO_ABOUT_DEFENDANT}" class="govuk-link">View the defendant legal representative contact details</a>. </p>',
        '<p class="govuk-body">Bydd angen i chi nawr gysylltu â''u cynrychiolaeth gyfreithiol.</p><p class="govuk-body"><a href="{VIEW_INFO_ABOUT_DEFENDANT}" class="govuk-link">Gweld manylion cyswllt cynrychiolydd cyfreithiol y diffynnydd</a>.</p>',
        'CLAIMANT');
