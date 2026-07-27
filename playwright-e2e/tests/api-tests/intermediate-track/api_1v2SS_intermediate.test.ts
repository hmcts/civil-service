import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS unspec intermediate track api journey', {
  tag: '@civil-service-nightly',
}, async () => {
  test('1v2 Same Solicitor Create Unspecified Intermediate Track claim', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
    JudgeApiSteps,
    HearingCenterAdminApiSteps
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimIntermediate1v2SS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondIntermediateFullDefence1v2SS();
    await ClaimantSolicitorApiSteps.RespondIntermediateProceed1v2SS();
    await JudgeApiSteps.GenerateDirectionsOrderIntermediate();
    await DefendantSolicitor1ApiSteps.EvidenceUploadFast1v2SS();
    await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
  });
});
