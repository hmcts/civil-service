/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.RequestedCCJ.Claimant','{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant",
        "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant",
        "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
        "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant",
        "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant",
        "Notice.AAA6.ClaimantIntent.SettlementNoResponse.Claimant",
        "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantAccepted.Claimant",
        "Notice.AAA6.ClaimantIntent.SettlementNoResponse.Claimant","Notice.AAA6.ClaimantIntent.Settlement.DefendantResponseAccepts.Claimant","Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant"}','{"Notice.AAA6.ClaimantIntent.RequestedCCJ.Claimant" : ["respondent1PartyName", "claimantRepaymentPlanDecision", "claimantRepaymentPlanDecisionCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.RequestedCCJ.Claimant', 'You requested a County Court Judgment against ${respondent1PartyName}', 'Rydych wedi gwneud cais am Ddyfarniad Llys Sirol (CCJ) yn erbyn ${respondent1PartyName}',
        '<p class="govuk-body">You ${claimantRepaymentPlanDecision} the <a href="{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}" class="govuk-link">repayment plan</a>. When we''ve processed the request, we''ll post a copy of the judgment to you.</p>',
        '<p class="govuk-body">Rydych wedi ${claimantRepaymentPlanDecisionCy} y <a href="{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}" class="govuk-link">cynllun ad-dalu</a>. Pan fyddwn wedi prosesu’r cais, byddwn yn anfon copi o’r dyfarniad drwy’r post atoch chi.</p>',
        'CLAIMANT');
