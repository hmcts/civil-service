import { test } from '../../../playwright-fixtures/index';

test.describe(
  '1v1 small case progression api journey',
  { tag: '@civil-service-nightly' },
  async () => {
    test('1v1 full defence unspecified - judge draws small claims WITH sum of damages - hearing scheduled', async ({
      ClaimantSolicitorApiSteps,
      CaseRoleAssignmentApiSteps,
      DefendantSolicitor1ApiSteps,
      HearingCenterAdminApiSteps,
      JudgeApiSteps,
    }) => {
      await ClaimantSolicitorApiSteps.CreateClaimSmall1v1();
      await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
      await ClaimantSolicitorApiSteps.AmendClaimDocuments();
      await ClaimantSolicitorApiSteps.NotifyClaim();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await ClaimantSolicitorApiSteps.NotifyClaimDetails();
      await DefendantSolicitor1ApiSteps.RespondSmallFullDefence();
      await ClaimantSolicitorApiSteps.RespondSmallProceed();
      await JudgeApiSteps.SdoSmallTrackSum();
      await ClaimantSolicitorApiSteps.EvidenceUploadSmall();
      await DefendantSolicitor1ApiSteps.EvidenceUploadSmall();
      await HearingCenterAdminApiSteps.ScheduleHearingSmallTrail();
      await HearingCenterAdminApiSteps.AmendHearingDueDate();
      await ClaimantSolicitorApiSteps.MakePaymentForHearingFee();
      await JudgeApiSteps.GenerateDirectionsOrderFreeFormOrder();
    });

    test('1v1 full defence unspecified - judge draws small claims WITHOUT sum of damages - hearing scheduled', async ({
      ClaimantSolicitorApiSteps,
      CaseRoleAssignmentApiSteps,
      DefendantSolicitor1ApiSteps,
      HearingCenterAdminApiSteps,
      JudgeApiSteps,
    }) => {
      await ClaimantSolicitorApiSteps.CreateClaimSmall1v1();
      await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
      await ClaimantSolicitorApiSteps.AmendClaimDocuments();
      await ClaimantSolicitorApiSteps.NotifyClaim();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await ClaimantSolicitorApiSteps.NotifyClaimDetails();
      await DefendantSolicitor1ApiSteps.RespondSmallFullDefence();
      await ClaimantSolicitorApiSteps.RespondSmallProceed();
      await JudgeApiSteps.SdoSmallTrackNoSum();
      await ClaimantSolicitorApiSteps.EvidenceUploadSmall();
      await DefendantSolicitor1ApiSteps.EvidenceUploadSmall();
      await HearingCenterAdminApiSteps.ScheduleHearingSmallTrail();
      await HearingCenterAdminApiSteps.AmendHearingDueDate();
      await ClaimantSolicitorApiSteps.MakePaymentForHearingFee();
    });
  },
);
