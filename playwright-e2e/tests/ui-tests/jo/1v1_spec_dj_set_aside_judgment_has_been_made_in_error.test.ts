import { test } from '../../../playwright-fixtures';

test.describe(
  '1v1 spec set aside judgment made in error',
  { tag: '@civil-ccd-nightly' },
  () => {
    test('1v1 spec default judgment then set aside judgment made in error and take case offline', async ({
      ClaimantSolicitorSpecApiSteps,
      CaseRoleAssignmentApiSteps,
      ClaimantSolicitorSpecSteps,
      HearingCenterAdminSpecSteps,
    }) => {
      await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
      await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await ClaimantSolicitorSpecApiSteps.AmendRespondent1ResponseDeadline();

      await ClaimantSolicitorSpecSteps.Login();
      await ClaimantSolicitorSpecSteps.RequestDefaultJudgment();

      await HearingCenterAdminSpecSteps.LoginRegion2();
      await HearingCenterAdminSpecSteps.RequestSetAsideJudgmentMadeInError();
      await HearingCenterAdminSpecSteps.CaseProceedsInCasemanSetAsideJudgment();
    });
  },
);
