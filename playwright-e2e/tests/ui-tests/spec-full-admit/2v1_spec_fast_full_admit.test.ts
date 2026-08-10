import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 Multi Party fast full admit claim', { tag: '@civil-ccd-nightly' }, () => {
  test('2v1 Multi Party fast full admit claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimFast2v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.RespondFastFullAdmit2v1();
  });
});
