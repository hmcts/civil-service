/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.Settle.ClaimPaidInFull.Defendant',
        '{}',
        '{"Notice.AAA6.Settle.ClaimPaidInFull.Defendant": ["settleClaimPaidInFullDateEn", "settleClaimPaidInFullDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy,
                                                   notification_role)
VALUES ('Notice.AAA6.Settle.ClaimPaidInFull.Defendant',
        'Claim marked as paid in full',
        'Hawliad wedi’i nodi fel wedi ei dalu’n llawn',
        '<p class="govuk-body">This claim has been marked as paid in full as of ${settleClaimPaidInFullDateEn}.<br>You do not need to attend court and any hearings scheduled will not go ahead.</p>',
        '<p class="govuk-body">Mae’r hawliad hwn wedi’i farcio fel un a dalwyd yn llawn ers ${settleClaimPaidInFullDateCy}.<br>Nid oes angen i chi fynychu''r llys ac ni fydd unrhyw wrandawiadau a drefnwyd yn cael eu cynnal.</p>',
        'DEFENDANT');
