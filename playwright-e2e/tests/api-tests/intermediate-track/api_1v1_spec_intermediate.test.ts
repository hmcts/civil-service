import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec intermediate track api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v1 spec full defence intermediate claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    JudgeApiSteps,
    HearingCenterAdminApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimIntermediate1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondIntermediateFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondIntermediateRejectFullDefence();
    await JudgeApiSteps.GenerateDirectionsOrderIntermediate();
    await DefendantSolicitor1SpecApiSteps.EvidenceUploadFast();
    await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
  });

  test('1v1 spec full admission intermediate claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimIntermediate1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondFullAdmitImmediately();
    await ClaimantSolicitorSpecApiSteps.RespondFullAdmitImmediately();
  });

  test('1v1 spec part admission intermediate claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimIntermediate1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondIntermediatePartAdmitImmediately();
    await ClaimantSolicitorSpecApiSteps.RespondIntermediateRejectPartAdmit();
  });

  test('1v1 spec counter claim intermediate claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimIntermediate1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondCounterClaim();
  });
});
