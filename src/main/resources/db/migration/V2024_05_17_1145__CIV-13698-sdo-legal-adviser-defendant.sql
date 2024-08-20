/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.SDOMadebyLA.Defendant',
        '{"Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant",
          "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant",
          "Notice.AAA6.ClaimantIntent.MediationUnsuccessful.Defendant"}',
        '{"Notice.AAA6.CP.SDOMadebyLA.Defendant" : ["requestForReconsiderationDeadlineEn", "requestForReconsiderationDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.SDOMadebyLA.Defendant', 'An order has been made on this claim', 'Mae gorchymyn wedi''i wneud ar yr hawliad hwn',
        '<p class="govuk-body">You need to carefully <a href="{VIEW_SDO_DOCUMENT}" rel="noopener noreferrer" target="_blank" class="govuk-link">read and review this order</a>. If you don''t agree with something in the order you can <a href="{REQUEST_FOR_RECONSIDERATION}" rel="noopener noreferrer" class="govuk-link">ask the court to review it</a>. You can only do this once. You will have to provide details about what changes you want made and these will be reviewed by a judge. This must be done before ${requestForReconsiderationDeadlineEn}.</p>',
        '<p class="govuk-body">Mae angen i chi <a href="{VIEW_SDO_DOCUMENT}" rel="noopener noreferrer" target="_blank" class="govuk-link">ddarllen ac adolygu''r gorchymyn hwn yn ofalus</a>. Os nad ydych yn cytuno â rhywbeth yn y gorchymyn, <a href="{REQUEST_FOR_RECONSIDERATION}" rel="noopener noreferrer" class="govuk-link">gallwch ofyn i''r llys ei adolygu</a>. Dim ond unwaith y gallwch wneud hyn. Bydd yn rhaid i chi roi manylion am y newidiadau rydych eisiau gweld yn cael eu gwneud a bydd y rhain yn cael eu hadolygu gan farnwr. Rhaid gwneud hyn cyn ${requestForReconsiderationDeadlineCy}.</p>',
        'DEFENDANT');

