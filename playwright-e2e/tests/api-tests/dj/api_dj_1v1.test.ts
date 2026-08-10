  import { test } from '../../../playwright-fixtures/index';

  test.describe('1v1 default judgement api journey', { tag: '@civil-service-nightly' }, async () => {
    test('1v1 default judgement api', async ({
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
      await ClaimantSolicitorApiSteps.DefaultJudgement();
    });
  });
