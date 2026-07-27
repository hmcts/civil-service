import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 - Manage Contact Information', async () => {
  test('1v1 - Manage Contact Information', async ({
    ClaimantSolicitorApiSteps, 
    CaseRoleAssignmentApiSteps, 
    DefendantSolicitor1ApiSteps,
    CaseworkerSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast1v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.AddLitigationFriend();
    await DefendantSolicitor1ApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorApiSteps.RespondFastProceed();
    await CaseworkerSteps.Login();
    await CaseworkerSteps.ManageContactInformation()
  });
})