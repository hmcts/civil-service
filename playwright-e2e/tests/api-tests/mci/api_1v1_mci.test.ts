import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 manage contact information api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v1 manage contact information', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast1v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorApiSteps.RespondFastProceed();
    await ClaimantSolicitorApiSteps.ManageContactInformation();
  });
});
