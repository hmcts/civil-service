/**
 * Update notification template
 */
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ClaimantIntent.CCJ.Requested.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestedCCJ.ClaimantAcceptedDefendantPlan.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant",
          "Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.DefPaymentMissed.Defendant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant",
          "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant",
          "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant"}';
where name = 'Scenario.AAA6.JudgmentsOnline.IssuedCCJ.Defendant';

UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">The judgment formalises the payment plan you’ve agreed with the claimant.<br>You’ve agreed to pay the claim amount of £${ccjDefendantAdmittedAmount} ${ccjPaymentMessageEn}. <br>The claimant’s details for payment and the full payment plan can be found on the judgment.<br>If you can no longer afford the repayments you’ve agreed with the claimant, you can make an application to vary the judgment.</p>',
  description_Cy = '<p class="govuk-body">Mae’r dyfarniad yn ffurfioli’r cynllun taliadau yr ydych wedi cytuno arno gyda’r hawlydd.<br>Rydych wedi cytuno i dalu’ swm yr hawliad, sef £${ccjDefendantAdmittedAmount} ${ccjPaymentMessageCy}. <br>Gellir dod o hyd i fanylion yr hawlydd ar gyfer talu a’r cynllun talu llawn ar y dyfarniad.<br>Os na allwch fforddio i dalu’r rhandaliadau rydych wedi cytuno arnynt gyda’r hawlydd, gallwch wneud cais i amrywio’r dyfarniad.</p>'
WHERE template_name = 'Notice.AAA6.JudgmentsOnline.IssuedCCJ.Defendant' AND notification_role = 'DEFENDANT';
