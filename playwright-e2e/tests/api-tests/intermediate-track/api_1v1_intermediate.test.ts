import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 unspec intermediate track api journey', {
  tag: '@civil-service-nightly',
}, async () => {
  test('1v1 Create Unspecified Intermediate Track claim', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
    JudgeApiSteps,
    HearingCenterAdminApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimIntermediate1v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondIntermediateFullDefence();
    await ClaimantSolicitorApiSteps.RespondIntermediateProceed();
    await JudgeApiSteps.GenerateDirectionsOrderIntermediate();
    await DefendantSolicitor1ApiSteps.EvidenceUploadFast();
    await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
  });
});
