import { test } from '../../../playwright-fixtures/index';

test.describe('1v2DS fast track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('1v2DS fast track claim journey', async ({
    ClaimantSolicitorSteps,
    DefendantSolicitor1Steps,
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor2Steps,
  }) => {
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.CreateClaimFast1v2DS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await ClaimantSolicitorSteps.NotifyClaim1v2DS();
    await ClaimantSolicitorSteps.NotifyClaimDetails1v2DS();
    await DefendantSolicitor1Steps.Login();
    await DefendantSolicitor1Steps.AcknowledgeClaimFullDefence();
    await DefendantSolicitor2Steps.Login();
    await DefendantSolicitor2Steps.AcknowledgeClaimFullDefence();
    await DefendantSolicitor1Steps.Login();
    await DefendantSolicitor1Steps.InformAgreedExtensionDate();
    await DefendantSolicitor1Steps.AddLitigationFriend();
    await DefendantSolicitor1Steps.RespondFastFullDefence1v2DS();
    await DefendantSolicitor2Steps.Login();
    await DefendantSolicitor2Steps.RespondFastFullDefence1v2DS();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.RespondFastProceed1v2DS();
  });
});
