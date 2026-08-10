import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec small part admit api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v1 spec small part admit setup before defendant response', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallPartAdmitImmediately();
    await ClaimantSolicitorSpecApiSteps.RespondSmallRejectPartAdmit();
  });
});
