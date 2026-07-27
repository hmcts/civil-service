import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 spec fast track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('2v1 spec fast track claim journey', async ({
    ClaimantSolicitorSpecSteps,
    DefendantSolicitor1SpecSteps,
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.CreateClaimFast2v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.RespondFastFullDefence2v1();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.RespondFastProceed2v1();
  });
});
