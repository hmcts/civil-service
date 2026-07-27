import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS spec api multi track journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v2SS spec full defence multi claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    JudgeApiSteps,
    HearingCenterAdminApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimMulti1v2SS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondMultiFullDefence1v2SS();
    await ClaimantSolicitorSpecApiSteps.RespondMultiRejectFullDefence1v2SS();
    await JudgeApiSteps.GenerateDirectionsOrderMulti();
    await ClaimantSolicitorSpecApiSteps.EvidenceUploadFast();
    await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
  });
});
