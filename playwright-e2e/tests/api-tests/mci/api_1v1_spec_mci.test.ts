import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec api manage contact information journeys', { tag: '@civil-service-nightly' }, async () => {
  test('1v1 spec api manage contact information', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    CaseworkerApiSteps
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondSmallRejectFullDefence();
    await CaseworkerApiSteps.ManageContactInformation();
  });
});
