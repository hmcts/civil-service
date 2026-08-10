import { test } from '../../../playwright-fixtures/index';

test.describe('1v2DS small track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('1v2DS small track claim journey', async ({
    ClaimantSolicitorSteps,
    DefendantSolicitor1Steps,
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor2Steps,
  }) => {
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.CreateClaimSmallTrack1v2DS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await ClaimantSolicitorSteps.NotifyClaim1v2DS();
    await ClaimantSolicitorSteps.NotifyClaimDetails1v2DS();
    await DefendantSolicitor1Steps.Login();
    await DefendantSolicitor1Steps.RespondSmallTrackFullDefence1v2DS();
    await DefendantSolicitor2Steps.Login();
    await DefendantSolicitor2Steps.RespondSmallTrackFullDefence1v2DS();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.RespondSmallProceed1v2DS();
  });
});
