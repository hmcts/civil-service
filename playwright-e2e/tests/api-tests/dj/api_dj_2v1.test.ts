import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 default judgement api journey', { tag: '@civil-service-nightly' }, async () => {
  test('2v1 default judgement api', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast2v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await ClaimantSolicitorApiSteps.AmendRespondent1ResponseDeadline();
    await ClaimantSolicitorApiSteps.DefaultJudgement();
  });
});
