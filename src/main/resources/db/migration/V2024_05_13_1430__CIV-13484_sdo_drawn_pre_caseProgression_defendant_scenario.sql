/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.SDODrawn.PreCaseProgression.Defendant',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant",
          "Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant"}', '{"Notice.AAA6.ClaimantIntent.SDODrawn.PreCaseProgression.Defendant" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.SDODrawn.PreCaseProgression.Defendant', 'An order has been issued by the court', 'Mae gorchymyn wedi’i gyhoeddi gan y llys',
        '<p class="govuk-body">Please follow instructions in the order and comply with the deadlines. Please send any documents to the court named in the order if required. The claim will now proceed offline, you will receive further updates by post.</p>',
        '<p class="govuk-body">Dilynwch y cyfarwyddiadau sydd yn y gorchymyn a chydymffurfiwch â’r dyddiadau terfyn. Anfonwch unrhyw ddogfennau i’r llys a enwir yn y gorchymyn os oes angen. Bydd yr hawliad nawr yn parhau all-lein a byddwch yn cael unrhyw ddiweddariadau pellach drwy’r post.</p>',
        'DEFENDANT');
