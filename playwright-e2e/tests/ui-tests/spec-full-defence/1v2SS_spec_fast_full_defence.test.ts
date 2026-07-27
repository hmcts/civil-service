import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS spec fast track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('1v2SS spec fast track claim journey', async ({
    DefendantSolicitor1SpecSteps,
    CaseRoleAssignmentApiSteps,
    ClaimantSolicitorApiSteps,
    ClaimantSolicitorSpecSteps,
  }) => {
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.CreateClaimFast1v2SS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.RespondFastFullDefence1v2SS();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.RespondFastProceed1v2SS();
  });
});
