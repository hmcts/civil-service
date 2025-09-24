import { test } from '../../playwright-fixtures';

test.describe('Specified Small track 1v2DS', async () => {
  test('Both Defendants Full Defence Claimant Intent To Proceed', async ({
    ClaimantSolicitorSpecSteps,
    DefendantSolicitor1SpecSteps,
    DefendantSolicitor2SpecSteps,
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    HearingCenterAdminSpecSteps,
  }) => {
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.CreateClaimSmallTrack1v2DS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.RespondSmallTrackFullDefence1v2DS();
    await DefendantSolicitor2SpecSteps.Login();
    await DefendantSolicitor2SpecSteps.RespondSmallTrackFullDefence1v2DS();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.RespondSmallClaimIntentToProceed1v2DS();
    await HearingCenterAdminSpecSteps.LoginRegion2();
    await HearingCenterAdminSpecSteps.CreateCaseLevelCaseFlag1v2DS();
    await HearingCenterAdminSpecSteps.CreateClaimant1CaseFlag1v2DS();
  });

  test('Default Judgment', async ({
    ClaimantSolicitorSpecSteps,
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.CreateClaimSmallTrack1v2DS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await ClaimantSolicitorSpecApiSteps.AmendRespondent1ResponseDeadline();
    await ClaimantSolicitorSpecApiSteps.AmendRespondent2ResponseDeadline();
    await ClaimantSolicitorSpecSteps.RequestDefaultJudgment1v2();
  });
});
