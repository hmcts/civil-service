import { test } from '../../../playwright-fixtures/index';

test.describe('Transfer Online Case 1v1 API test - fast claim - unspec', { tag: '@civil-service-nightly' }, async () => {
  test('1v1 unspec full defence - not suitable SDO - Transfer Case', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    CaseworkerApiSteps,
    DefendantSolicitor1ApiSteps,
    JudgeApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast1v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorApiSteps.RespondFastProceed();
    await JudgeApiSteps.NotSuitableSdoChangeLocation();
    await CaseworkerApiSteps.TransferOnlineCase();
  });

  test('1v1 unspec full defence - not suitable SDO - Other Reasons', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
    JudgeApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast1v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorApiSteps.RespondFastProceed();
    await JudgeApiSteps.NotSuitableSdoOther();
  });
});
