import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 Spec - Manage Contact Information', async () => {
  test('1v1 Spec - Manage Contact Information', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    CaseworkerSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimFast1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondFastRejectFullDefence();
    await CaseworkerSteps.Login();
    await CaseworkerSteps.ManageContactInformationSpec();
  });
})