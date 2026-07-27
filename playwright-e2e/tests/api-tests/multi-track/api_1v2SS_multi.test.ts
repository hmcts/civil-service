import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS unspec multi track journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v2SS unspec multi track claim', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
    JudgeApiSteps,
    HearingCenterAdminApiSteps
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimMulti1v2SS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondMultiFullDefence1v2SS();
    await ClaimantSolicitorApiSteps.RespondMultiProceed1v2SS();
    await JudgeApiSteps.GenerateDirectionsOrderMulti();
    await ClaimantSolicitorApiSteps.EvidenceUploadFast();
    await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
  });
});
