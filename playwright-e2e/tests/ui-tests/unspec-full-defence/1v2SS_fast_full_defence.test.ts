import { test } from '../../../playwright-fixtures/index';

test.describe('1v2SS fast track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('1v2SS fast track claim journey', async ({
    ClaimantSolicitorSteps,
    DefendantSolicitor1Steps,
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.CreateClaimFast1v2SS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await ClaimantSolicitorSteps.NotifyClaimDetails();
    await DefendantSolicitor1Steps.Login();
    await DefendantSolicitor1Steps.AcknowledgeClaimFullDefence1v2SS();
    await DefendantSolicitor1Steps.InformAgreedExtensionDate();
    await DefendantSolicitor1Steps.AddLitigationFriend1v2SS();
    await DefendantSolicitor1Steps.RespondFastFullDefence1v2SS();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.RespondFastProceed1v2SS();
  });
});
