import { test } from '../../../playwright-fixtures/index';

test.describe('Transfer Online Case 1v2 API test - fast claim - unspec @debug', { tag: '@civil-service-nightly' }, async () => {
  test('1v2 full defence unspecified - not suitable SDO - Transfer Case)', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    CaseworkerApiSteps,
    DefendantSolicitor1ApiSteps,
    DefendantSolicitor2ApiSteps,
    JudgeApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast1v2DS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondFastFullDefence1v2DS();
    await DefendantSolicitor2ApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorApiSteps.RespondFastProceed1v2DS();
    await JudgeApiSteps.NotSuitableSdoChangeLocation();
    await CaseworkerApiSteps.TransferOnlineCase();
  });
});
