import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS spec api small track journeys', { tag: '@civil-service-nightly' }, async () => {
  test('1v2SS small claim full defence and claimant response', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v2SS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence1v2SS();
    await ClaimantSolicitorSpecApiSteps.RespondFastRejectFullDefence1v2SS();
  });
});
