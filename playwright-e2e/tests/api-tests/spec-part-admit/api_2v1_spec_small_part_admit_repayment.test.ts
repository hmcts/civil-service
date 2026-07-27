import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 spec small part admit api journey', { tag: '@civil-service-nightly' }, async () => {
  test('2v1 spec small part admit setup before defendant response', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack2v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallPartAdmitRepayment2v1();
    // await ClaimantSolicitorSpecApiSteps.RespondSmallRejectPartAdmit();
  });
});
