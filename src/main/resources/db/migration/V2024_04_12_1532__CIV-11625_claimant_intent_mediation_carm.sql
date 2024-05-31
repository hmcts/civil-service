/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.Mediation.CARM.Claimant',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant",
         "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant",
         "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Claimant",
         "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
         "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant",
         "Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant",
         "Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant",
         "Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant",
         "Notice.AAA6.ClaimantIntent.Mediation.Claimant",
         "Notice.AAA6.ClaimantIntent.GoToHearing.Claimant"}',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Claimant" : []}');

/**
 * Add notification template for claimant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.Mediation.CARM.Claimant',
        'Your claim is now going to mediation',
        'Mae eich hawliad nawr yn mynd i gyfryngu',
        '<p class="govuk-body">Your claim is now going to mediation. You will be contacted within 28 days with details of your appointment. <br> If you do not attend your mediation appointment, the judge may issue a penalty.</p>',
        '<p class="govuk-body">Mae eich hawliad nawr yn mynd i gyfryngu. Byddwn yn cysylltu â chi o fewn 28 diwrnod gyda manylion am eich apwyntiad. <br> Os na fyddwch yn mynychu’ch apwyntiad cyfryngu, efallai y bydd y barnwr yn eich cosbi.</p>',
        'CLAIMANT');

/**
 * Add scenario for defendant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimantIntent.Mediation.CARM.Defendant',
        '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Defendant",
         "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Defendant",
         "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstallments.Defendant",
         "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant",
         "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Defendant",
         "Notice.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant",
         "Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant",
         "Notice.AAA6.ClaimantIntent.Mediation.Defendant",
         "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant",
         "Notice.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant",
         "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant",
         "Notice.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant"}',
        '{"Notice.AAA6.ClaimantIntent.Mediation.CARM.Defendant" : []}');

/**
 * Add notification template for defendant
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimantIntent.Mediation.CARM.Defendant',
        'Your claim is now going to mediation',
        'Mae eich hawliad nawr yn mynd i gyfryngu',
        '<p class="govuk-body">Your claim is now going to mediation. You will be contacted within 28 days with details of your appointment. <br> If you do not attend your mediation appointment, the judge may issue a penalty.</p>',
        '<p class="govuk-body">Mae eich hawliad nawr yn mynd i gyfryngu. Byddwn yn cysylltu â chi o fewn 28 diwrnod gyda manylion am eich apwyntiad. <br> Os na fyddwch yn mynychu’ch apwyntiad cyfryngu, efallai y bydd y barnwr yn eich cosbi.</p>',
        'DEFENDANT');
