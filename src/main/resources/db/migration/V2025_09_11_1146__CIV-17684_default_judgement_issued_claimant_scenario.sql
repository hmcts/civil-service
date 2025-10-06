/**
 * Update notification template
 */
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Claimant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.Response.Required"}'
where name = 'Scenario.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant';

UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">The defendant should now pay you according to the terms of the judgment. <br> Once they do, you should <a href="{CONFIRM_YOU_HAVE_BEEN_PAID_URL}" class="govuk-link">confirm that they’ve paid you the full amount that you’re owed</a>.<br>If they do not pay you by the date on the judgment, you can <u>ask for enforcement action to be taken against them</u>. <br>If you need to change the terms of payment within the judgment, such as the instalments you had previously agreed, you can ${djClaimantNotificationMessage}.</p>',
  description_Cy = '<p class="govuk-body">Dylai’r diffynnydd eich talu yn unol â thelerau’r dyfarniad. <br> Unwaith y byddant yn gwneud hynny, dylech <a href="{CONFIRM_YOU_HAVE_BEEN_PAID_URL}" class="govuk-link">gadarnhau eu bod wedi talu’r swm llawn sy’n ddyledus i chi</a>.<br>Os na fyddant yn eich talu erbyn y dyddiad ar y dyfarniad, gallwch <u>ofyn am gymryd camau  gorfodi yn eu herbyn</u>. <br>Os oes arnoch angen newid y telerau talu o fewn y dyfarniad, fel y rhandaliadau roeddech wedi cytuno arnynt yn flaenorol, gallwch ${djClaimantNotificationMessageCy}.</p>'
WHERE template_name = 'Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Claimant' AND notification_role = 'CLAIMANT';
