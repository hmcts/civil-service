/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.Bundle.Updated.Claimant',
        '{"Notice.AAA6.CP.Bundle.Ready.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Claimant", "Notice.AAA6.CP.Trial.Arrangements.Required.Claimant"}',
        '{"Notice.AAA6.CP.Bundle.Updated.Claimant": ["bundleRestitchedDateEn", "bundleRestitchedDateCy"]}'),
       ('Scenario.AAA6.CP.Bundle.Updated.Defendant',
        '{"Notice.AAA6.CP.Bundle.Ready.Defendant", "Notice.AAA6.CP.Trial.Arrangements.Finalised.NotifyOtherParty.Defendant", "Notice.AAA6.CP.Trial.Arrangements.Required.Defendant"}',
        '{"Notice.AAA6.CP.Bundle.Updated.Defendant": ["bundleRestitchedDateEn", "bundleRestitchedDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role, time_to_live)
VALUES ('Notice.AAA6.CP.Bundle.Updated.Claimant',
        'The case bundle has been updated',
        'Mae bwndel yr achos wedi''i ddiweddaru',
        '<p class="govuk-body">The case bundle was changed and re-uploaded on ${bundleRestitchedDateEn}. <a href="{VIEW_BUNDLE_REDIRECT}" class="govuk-link">Review the new bundle</a>.</p>',
        '<p class="govuk-body">Cafodd bwndel yr achos ei newid a''i ail-uwchlwytho ar ${bundleRestitchedDateCy}. <a href="{VIEW_BUNDLE_REDIRECT}" class="govuk-link">Adolygu’r bwndel newydd</a>.</p>',
        'CLAIMANT', 'Session'),
       ('Notice.AAA6.CP.Bundle.Updated.Defendant',
        'The case bundle has been updated',
        'Mae bwndel yr achos wedi''i ddiweddaru',
        '<p class="govuk-body">The case bundle was changed and re-uploaded on ${bundleRestitchedDateEn}. <a href="{VIEW_BUNDLE_REDIRECT}" class="govuk-link">Review the new bundle</a>.</p>',
        '<p class="govuk-body">Cafodd bwndel yr achos ei newid a''i ail-uwchlwytho ar ${bundleRestitchedDateCy}. <a href="{VIEW_BUNDLE_REDIRECT}" class="govuk-link">Adolygu’r bwndel newydd</a>.</p>',
        'DEFENDANT', 'Session');
