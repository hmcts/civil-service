import { test } from '../../../playwright-fixtures/index';

test.describe('1v2DS spec fast track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('1v2DS spec fast track claim journey', async ({
    DefendantSolicitor1SpecSteps,
    CaseRoleAssignmentApiSteps,
    ClaimantSolicitorApiSteps,
    DefendantSolicitor2SpecSteps,
    ClaimantSolicitorSpecSteps,
  }) => {
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.CreateClaimFast1v2DS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.RespondFastFullDefence1v2DS();
    await DefendantSolicitor2SpecSteps.Login();
    await DefendantSolicitor2SpecSteps.RespondFastFullDefence1v2DS();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.RespondFastProceed1v2DS();
  });
});
