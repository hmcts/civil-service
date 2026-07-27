import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 default judgment', { tag: '@civil-ccd-nightly' }, () => {
  test('1v1 default judgment', async ({
    ClaimantSolicitorSteps,
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimSmall1v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await ClaimantSolicitorApiSteps.AmendRespondent1ResponseDeadline();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.RequestDefaultJudgment();
  });
});
