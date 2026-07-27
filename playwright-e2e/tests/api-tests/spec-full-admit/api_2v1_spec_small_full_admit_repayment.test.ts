import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 spec small full admit repayment api journey', { tag: '@civil-service-nightly' }, async () => {
  test('2v1 spec small full admit repayment', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack2v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondFullAdmitRepayment2v1();
    // await ClaimantSolicitorSpecApiSteps.RespondFullAdmitRepayment();
  });
});
