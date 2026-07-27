import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec default judgment', { tag: '@civil-ccd-nightly' },
  () => {
    test('1v1 spec default judgment', async ({
      ClaimantSolicitorSpecSteps,
      ClaimantSolicitorSpecApiSteps,
      CaseRoleAssignmentApiSteps,
    }) => {
      await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
      await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await ClaimantSolicitorSpecApiSteps.AmendRespondent1ResponseDeadline();
      await ClaimantSolicitorSpecSteps.Login();
      await ClaimantSolicitorSpecSteps.RequestDefaultJudgment();
    });
})
