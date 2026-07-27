import { test } from '../../../playwright-fixtures/index';

test.describe(
  '1v1 spec defence received in time judgment set aside',
  { tag: '@civil-ccd-nightly' },
  () => {
    test('1v1 spec default judgment then refer to judge defended claim and make an order', async ({
      ClaimantSolicitorSpecApiSteps,
      CaseRoleAssignmentApiSteps,
      ClaimantSolicitorSpecSteps,
      HearingCenterAdminSpecSteps,
      JudgeSteps,
    }) => {
      await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
      await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await ClaimantSolicitorSpecApiSteps.AmendRespondent1ResponseDeadline();

      await ClaimantSolicitorSpecSteps.Login();
      await ClaimantSolicitorSpecSteps.RequestDefaultJudgment();

      await HearingCenterAdminSpecSteps.LoginRegion2();
      await HearingCenterAdminSpecSteps.RequestReferJudgeDefenceReceived();

      // Uncomment after DTSCCI-5517 fixed
      // await JudgeSteps.LoginRegion2();
      // await JudgeSteps.GenerateDirectionsOrderFreeForm();
    });
  },
);
