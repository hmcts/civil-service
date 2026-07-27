import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 spec fast part admit api journey', { tag: '@civil-service-nightly' }, async () => {
  test('2v1 spec fast part admit setup before defendant response', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimFast2v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondFastPartAdmitRepayment2v1();
    // await ClaimantSolicitorSpecApiSteps.RespondFastRejectPartAdmit();
  });
});
