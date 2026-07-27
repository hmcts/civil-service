import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec api multi track journeys', { tag: '@civil-service-nightly' }, async () => {
  test('1v1 spec full defence multi claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    JudgeApiSteps,
    HearingCenterAdminApiSteps
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimMulti1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondMultiFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondMultiRejectFullDefence();
    await JudgeApiSteps.GenerateDirectionsOrderMulti();
    await ClaimantSolicitorSpecApiSteps.EvidenceUploadFast();
    await HearingCenterAdminApiSteps.ScheduleHearingFastTrial();
  });

  test('1v1 spec full admission multi claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimMulti1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondMultiFullAdmitImmediately();
    await ClaimantSolicitorSpecApiSteps.RespondFullAdmitImmediately();
  });

  test('1v1 spec part admission multi claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimMulti1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondMultiPartAdmitImmediately();
    await ClaimantSolicitorSpecApiSteps.RespondMultiRejectPartAdmit();
  });

  test('1v1 spec counter claim multi claim', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimMulti1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondMultiCounterClaim();
  });
});
