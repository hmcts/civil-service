import { test } from '../../../playwright-fixtures/index';

test.describe('1v2DS unspec multi track journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v2DS unspec multi track claim', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
    DefendantSolicitor2ApiSteps,
    JudgeApiSteps,
    HearingCenterAdminApiSteps
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimMulti1v2DS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondMultiFullDefence();
    await DefendantSolicitor2ApiSteps.RespondMultiFullDefence();
    await ClaimantSolicitorApiSteps.RespondMultiProceed1v2DS();
    await JudgeApiSteps.GenerateDirectionsOrderMulti();
    await ClaimantSolicitorApiSteps.EvidenceUploadFast();
    await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
  });
});
