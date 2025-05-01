/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant', '{"Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Defendant"}', '{"Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant" : ["applicant1PartyName", "claimantRepaymentPlanDecision", "claimantRepaymentPlanDecisionCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.RequestCCJ.ClaimantAcceptOrRejectPlan.SettlementRequested.NoDefResponse.Defendant', '${applicant1PartyName} has requested a County Court Judgment against you',
        'Mae ${applicant1PartyName} wedi gwneud cais am Ddyfarniad Llys Sirol yn eich erbyn',
        '<p class="govuk-body">${applicant1PartyName} ${claimantRepaymentPlanDecision} your repayment plan and asked you to sign a settlement. You did not sign the agreement.</p>'
        '<p class="govuk-body">When we''ve processed the request, we''ll post a copy of the judgment to you.</p>'
        '<p class="govuk-body">If you pay the debt within one month of the date of judgment, the County Court Judgment (CCJ) is removed from the public register. You can pay £15 to <a href={APPLY_FOR_CERTIFICATE} class="govuk-link" target="_blank" rel="noopener noreferrer">apply for a certificate (opens in new tab)</a> that confirms this.</p>'
        '<p class="govuk-body"><a href="{CITIZEN_CONTACT_THEM_URL}" class="govuk-link">Contact ${applicant1PartyName}</a> if you need their payment details.</p><p class="govuk-body"><a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">View your response</a></p>',
        '<p class="govuk-body">Mae ${applicant1PartyName} wedi ${claimantRepaymentPlanDecisionCy} eich cynllun ad-dalu ac wedi gofyn i chi lofnodi setliad. Ni wnaethoch lofnodi’r cytundeb.</p>'
        '<p class="govuk-body">Pan fyddwn wedi prosesu’r cais, byddwn yn anfon copi o’r dyfarniad drwy''r post atoch chi.</p>'
        '<p class="govuk-body">Os byddwch yn talu’r ddyled o fewn mis o ddyddiad y dyfarniad, bydd y Dyfarniad Llys Sirol (CCJ) yn cael ei ddileu o’r gofrestr gyhoeddus. Gallwch dalu £15 i <a href={APPLY_FOR_CERTIFICATE} class="govuk-link" target="_blank" rel="noopener noreferrer"> wneud cais am dystysgrif (yn agor mewn tab newydd)</a> sy’n cadarnhau hyn.</p>'
        '<p class="govuk-body"><a href="{CITIZEN_CONTACT_THEM_URL}" class="govuk-link">Cysylltwch â ${applicant1PartyName}</a> os oes arnoch angen eu manylion talu.</p><p class="govuk-body"><a href={VIEW_RESPONSE_TO_CLAIM} class="govuk-link">Gweld eich ymateb</a></p>',
        'DEFENDANT');
