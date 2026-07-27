import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec discontinue claim', { tag: ['@civil-ccd-nightly'] }, () => {
  test('1v1 spec discontinue this claim - full discontinuance', async ({
    ClaimantSolicitorSpecSteps,
    ClaimantSolicitorSpecApiSteps,
    DefendantSolicitor1SpecApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondSmallRejectFullDefence();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.DiscontinueClaim1v1();
  });
});
