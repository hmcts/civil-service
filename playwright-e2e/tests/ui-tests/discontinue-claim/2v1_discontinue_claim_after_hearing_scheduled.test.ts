import { test } from '../../../playwright-fixtures/index';

test.describe(
  '2v1 discontinue claim after hearing scheduled',
  { tag: ['@civil-ccd-nightly'] },
  () => {
    test('2v1 discontinue this claim after hearing schedule - full discontinuance', async ({
      ClaimantSolicitorSteps,
      ClaimantSolicitorApiSteps,
      DefendantSolicitor1ApiSteps,
      JudgeApiSteps,
      HearingCenterAdminApiSteps,
      CaseRoleAssignmentApiSteps,
    }) => {
      await ClaimantSolicitorApiSteps.CreateClaimFast2v1();
      await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
      await ClaimantSolicitorApiSteps.NotifyClaim();
      await ClaimantSolicitorApiSteps.NotifyClaimDetails();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await DefendantSolicitor1ApiSteps.RespondFastFullDefence2v1();
      await ClaimantSolicitorApiSteps.RespondFastProceed2v1();
      await JudgeApiSteps.SdoFast();
      await HearingCenterAdminApiSteps.ScheduleHearingFastTrialWA();
      await ClaimantSolicitorSteps.Login();
      await ClaimantSolicitorSteps.DiscontinueClaim2v1();
    });
  },
);
