/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefendantNoticeOfChange.ClaimMovesOffline.Claimant',
        '{"Notice.AAA6.DefendantNoticeOfChange.ClaimRemainsOnline.Claimant",
          "Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant",
          "Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant",
          "Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant",
          "Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant",
          "Notice.AAA6.ClaimantIntent.SettlementNoResponse.Claimant",
          "Notice.AAA6.ClaimantIntent.Settlement.DefendantResponseAccepts.Claimant",
          "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant",
          "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
          "Notice.AAA6.ClaimIssue.Response.Await",
          "Notice.AAA6.ClaimIssue.HWF.PhonePayment",
          "Notice.AAA6.DefResponse.MoretimeRequested.Claimant",
          "Notice.AAA6.ClaimIssue.HWF.FullRemission"}',
        '{"Notice.AAA6.DefendantNoticeOfChange.ClaimMovesOffline.Claimant" : ["respondent1PartyName", "applicant1ResponseDeadlineEn", "applicant1ResponseDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.DefendantNoticeOfChange.ClaimMovesOffline.Claimant',
        '${respondent1PartyName} has asked for a legal representative to act on their behalf',
        'Mae ${respondent1PartyName} wedi gofyn i gynrychiolydd cyfreithiol weithredu ar eu rhanh',
        '<p class="govuk-body">${respondent1PartyName} has asked for a legal representative to act on their behalf. From now on you will need to liaise with their representative.<br>'
        '<a href="{VIEW_INFO_ABOUT_DEFENDANT}" class="govuk-link">View the contact details of the defendant’s legal representative.</a><br>'
        'This claim will now move offline and you must submit your intention to proceed by using form <a href="https://www.gov.uk/government/publications/form-n225-request-for-judgment-and-reply-to-admission-specified-amount" target="_blank" class="govuk-link">N225</a> (for a full admission) or <a href="https://www.gov.uk/government/publications/form-n225a-notice-of-part-admission-specified-amount" target="_blank" class="govuk-link">N225A</a> (for a partial admission) by ${applicant1ResponseDeadlineEn}.</p>',
        '<p class="govuk-body">Mae ${respondent1PartyName} wedi gofyn i gynrychiolydd cyfreithiol weithredu ar eu rhan.  O hyn ymlaen bydd angen i chi gysylltu â''u cynrychiolydd.<br>'
        '<a href="{VIEW_INFO_ABOUT_DEFENDANT}" class="govuk-link">Gweld manylion cyswllt cynrychiolydd cyfreithiol y diffynnydd.</a><br>'
        'Bydd yr hawliad hwn bellach yn symud all-lein ac mae''n rhaid i chi gyflwyno eich bwriad i fwrw ymlaen trwy ddefnyddio ffurflen <a href="https://www.gov.uk/government/publications/form-n225-request-for-judgment-and-reply-to-admission-specified-amount" target="_blank" class="govuk-link">N225</a> (ar gyfer addefiad llawn) neu <a href="https://www.gov.uk/government/publications/form-n225a-notice-of-part-admission-specified-amount" target="_blank" class="govuk-link">N225A</a> (ar gyfer addefiad rhannol) erbyn ${applicant1ResponseDeadlineCy}.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">View the response to the claim</a>', 'The response',
        '<a href={VIEW_RESPONSE_TO_CLAIM} rel="noopener noreferrer" class="govuk-link">Gweld yr ymateb i''r hawliad</a>',
        'Yr ymateb', 'Response.View', 'Scenario.AAA6.DefendantNoticeOfChange.ClaimMovesOffline.Claimant', '{3, 3}', 'CLAIMANT', 3),
       ('<a>Contact the court to request a change to my case</a>', 'Applications',
        '<a>Contact the court to request a change to my case</a>',
        'Ceisiadau', 'Application.Create', 'Scenario.AAA6.DefendantNoticeOfChange.ClaimMovesOffline.Claimant', '{2, 2}', 'CLAIMANT', 16),
       ('<a>View applications</a>', 'Applications' ,'<a>Gweld y cais i gyd</a>',
        'Ceisiadau', 'Application.View', 'Scenario.AAA6.DefendantNoticeOfChange.ClaimMovesOffline.Claimant', '{2, 2}', 'CLAIMANT', 17);
