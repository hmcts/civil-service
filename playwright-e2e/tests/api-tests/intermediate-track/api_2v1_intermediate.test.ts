import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 unspec intermediate track api journey', {
  tag: '@civil-service-nightly',
}, async () => {
  test('2v1 Create Unspecified Intermediate Track claim', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
    JudgeApiSteps,
    HearingCenterAdminApiSteps
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimIntermediate2v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondIntermediateFullDefence2v1();
    await ClaimantSolicitorApiSteps.RespondIntermediateProceed2v1();
    await JudgeApiSteps.GenerateDirectionsOrderIntermediate();
    await DefendantSolicitor1ApiSteps.EvidenceUploadFast();
    await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
  });
});
