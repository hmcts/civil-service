import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 unspec multi track journey', { tag: '@civil-service-nightly' }, async () => {
  test('2v1 unspec multi track claim', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
    JudgeApiSteps,
    HearingCenterAdminApiSteps
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimMulti2v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondMultiFullDefence2v1();
    await ClaimantSolicitorApiSteps.RespondMultiProceed2v1();
    await JudgeApiSteps.GenerateDirectionsOrderMulti();
    await ClaimantSolicitorApiSteps.EvidenceUploadFast2v1();
    await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
  });
});
