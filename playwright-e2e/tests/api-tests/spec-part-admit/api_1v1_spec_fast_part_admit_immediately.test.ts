import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec part admit api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v1 spec part admit', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimFast1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondFastPartAdmitImmediately();
    await ClaimantSolicitorSpecApiSteps.RespondFastRejectPartAdmit();
  });
});
