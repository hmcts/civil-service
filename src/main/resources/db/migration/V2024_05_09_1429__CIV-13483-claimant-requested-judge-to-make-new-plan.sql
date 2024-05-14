/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.RequestJudgePlan.RequestedCCJ.Claimant',
        '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant","Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.RequestJudgePlan.RequestedCCJ.Claimant" : ["respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.RequestJudgePlan.RequestedCCJ.Claimant',
        'You requested a County Court Judgment against ${respondent1PartyName}',
        'Rydych wedi gwneud cais am Ddyfarniad Llys Sirol yn erbyn ${respondent1PartyName}',
        '<p class="govuk-body">You rejected the <a href="{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}" class="govuk-link">repayment plan</a>.</p><p class="govuk-body">When a judge has made a decision, we’ll post a copy of the judgment to you.</p>',
        '<p class="govuk-body">Rydych wedi gwrthod y <a href="{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}" class="govuk-link">cynllun ad-dalu</a>.</p><p class="govuk-body">Pan fydd barnwr wedi gwneud penderfyniad, byddwn yn anfon copi o’r dyfarniad drwy’r post atoch chi.</p>',
        'CLAIMANT');
