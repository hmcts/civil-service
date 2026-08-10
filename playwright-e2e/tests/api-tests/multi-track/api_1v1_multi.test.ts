import { test } from '../../../playwright-fixtures/index';

test.describe(
  '1v1 unspec multi track journey',
  { tag: '@civil-service-nightly' },
  async () => {
    test('1v1 unspec multi track', async ({
      ClaimantSolicitorApiSteps,
      CaseRoleAssignmentApiSteps,
      DefendantSolicitor1ApiSteps,
      JudgeApiSteps,
      HearingCenterAdminApiSteps
    }) => {
      await ClaimantSolicitorApiSteps.CreateClaimMulti1v1();
      await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
      await ClaimantSolicitorApiSteps.NotifyClaim();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await ClaimantSolicitorApiSteps.NotifyClaimDetails();
      await DefendantSolicitor1ApiSteps.RespondMultiFullDefence();
      await ClaimantSolicitorApiSteps.RespondMultiProceed();
      await JudgeApiSteps.GenerateDirectionsOrderMulti();
      await ClaimantSolicitorApiSteps.EvidenceUploadFast();
      await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
    });
  },
);
