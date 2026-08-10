import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS spec fast counter claim api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v2SS spec fast counter claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimFast1v2SS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondCounterClaim1v2SS();
  });
});
