INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.DefendantNoticeOfChange.JudgmentByAdmissionClaimMovesOffline.Claimant',
        '{"Notice.AAA6.DefendantNoticeOfChange.ClaimRemainsOnline.Claimant", "Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant",
          "Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
          "Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant",
          "Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant", "Notice.AAA6.ClaimantIntent.SettlementNoResponse.Claimant",
          "Notice.AAA6.ClaimantIntent.Settlement.DefendantResponseAccepts.Claimant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant",
          "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimIssue.Response.Await",
          "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.DefResponse.MoretimeRequested.Claimant", "Notice.AAA6.ClaimIssue.HWF.FullRemission"}',
        '{"Notice.AAA6.DefendantNoticeOfChange.JudgmentByAdmissionClaimMovesOffline.Claimant" : ["respondent1PartyName"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy ,notification_role)
VALUES ('Notice.AAA6.DefendantNoticeOfChange.JudgmentByAdmissionClaimMovesOffline.Claimant',
        '${respondent1PartyName} has asked for a legal representative to act on their behalf',
        'Mae ${respondent1PartyName} wedi gofyn i gynrychiolydd cyfreithiol weithredu ar eu rhan',
        '<p class="govuk-body">${respondent1PartyName} has asked for a legal representative to act on their behalf. From now on you will need to liaise with their representative.<br>'
        '<a href="{VIEW_INFO_ABOUT_DEFENDANT}" class="govuk-link">View the contact details of the defendant''s legal representative</a>.<br>'
        'This claim will now move offline.</p>',
        '<p class="govuk-body">${respondent1PartyName} wedi gofyn i gynrychiolydd cyfreithiol weithredu ar eu rhan.  O hyn ymlaen bydd angen i chi gysylltu â''u cynrychiolydd.<br>'
        '<a href="{VIEW_INFO_ABOUT_DEFENDANT}" class="govuk-link">Gweld manylion cyswllt cynrychiolydd cyfreithiol y diffynnydd</a>.<br>'
        'Bydd yr hawliad hwn nawr yn symud i fod all-lein.</p>',
        'CLAIMANT');
