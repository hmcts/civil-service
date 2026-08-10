import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 spec discontinue claim', { tag: ['@civil-ccd-nightly'] }, () => {
  test('2v1 spec discontinue this claim - full discontinuance', async ({
    ClaimantSolicitorSpecSteps,
    ClaimantSolicitorSpecApiSteps,
    DefendantSolicitor1SpecSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack2v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.RespondSmallTrackFullDefence2v1();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.RespondSmallProceed2v1();
    await ClaimantSolicitorSpecSteps.DiscontinueClaim2v1();
  });
});
