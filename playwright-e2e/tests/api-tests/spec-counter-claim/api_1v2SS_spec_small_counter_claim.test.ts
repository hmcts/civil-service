import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS spec small counter claim api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v2SS spec small counter claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v2SS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondCounterClaim1v2SS();
  });
});
