import { test } from '../../../playwright-fixtures/index';

test.describe('1v2DS spec small full defence api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v2DS spec small full defence', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    DefendantSolicitor2SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v2DS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence1v2DS();
    await DefendantSolicitor2SpecApiSteps.RespondSmallFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondSmallRejectFullDefence1v2DS();
  });
});
