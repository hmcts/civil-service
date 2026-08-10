import { test } from '../../../playwright-fixtures/index';

test.describe('1v2DS spec full defence api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v2DS spec full defence', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    DefendantSolicitor2SpecApiSteps
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimFast1v2DS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await DefendantSolicitor1SpecApiSteps.RespondFastFullDefence1v2DS();
    await DefendantSolicitor2SpecApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondFastRejectFullDefence();
  });
});
