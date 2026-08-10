import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS spec small part admit api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v2SS spec small part admit setup before defendant response', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v2SS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallPartAdmitSetDate1v2SS();
    // await ClaimantSolicitorSpecApiSteps.RespondSmallRejectPartAdmit();
  });
});
