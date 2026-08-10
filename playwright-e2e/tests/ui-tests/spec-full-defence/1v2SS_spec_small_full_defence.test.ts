import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS spec small track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('1v2SS spec small track claim journey', async ({
    ClaimantSolicitorSpecSteps,
    DefendantSolicitor1SpecSteps,
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.CreateClaimSmallTrack1v2SS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.RespondSmallTrackFullDefence1v2SS();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.RespondSmallProceed1v2SS();
  });
});
