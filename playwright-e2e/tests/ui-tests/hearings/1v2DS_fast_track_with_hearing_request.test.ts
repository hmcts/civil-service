import { test } from '../../../playwright-fixtures/index';

test.describe(
  '1v2DS fast track with hearing request',
  {
    tag: ['@civil-ccd-nightly'],
  },
  () => {
    test('1v2DS create fast track with hearing request', async ({
      ClaimantSolicitorApiSteps,
      CaseRoleAssignmentApiSteps,
      DefendantSolicitor1ApiSteps,
      DefendantSolicitor2ApiSteps,
      JudgeApiSteps,
      HearingCenterAdminSteps,
    }) => {
      await ClaimantSolicitorApiSteps.CreateClaimFast1v2DS();
      await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
      await ClaimantSolicitorApiSteps.NotifyClaim();
      await ClaimantSolicitorApiSteps.NotifyClaimDetails();
      await DefendantSolicitor1ApiSteps.RespondFastFullDefence1v2DS();
      await DefendantSolicitor2ApiSteps.RespondFastFullDefence();
      await ClaimantSolicitorApiSteps.RespondFastProceed1v2DS();;
      await JudgeApiSteps.SdoFast();
      await HearingCenterAdminSteps.LoginRegion1();
      await HearingCenterAdminSteps.RequestNewHearing();
      await HearingCenterAdminSteps.UpdateHearing();
      await HearingCenterAdminSteps.CancelHearing();
    });
  },
);
