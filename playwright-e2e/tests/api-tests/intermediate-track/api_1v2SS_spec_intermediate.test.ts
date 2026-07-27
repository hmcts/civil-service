import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS spec intermediate track api journey', {
  tag: '@civil-service-nightly',
}, async () => {
  test('1v2SS spec full defence intermediate claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimIntermediate1v2SS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondIntermediateFullDefence1v2SS();
    await ClaimantSolicitorSpecApiSteps.RespondIntermediateRejectFullDefence();
  });
});
