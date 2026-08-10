import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec fast track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('1v1 spec fast track claim journey', async ({
    ClaimantSolicitorSteps,
    DefendantSolicitor1Steps,
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.CreateClaimFast1v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorSteps.NotifyClaimDetails();
    await DefendantSolicitor1Steps.Login();
    await DefendantSolicitor1Steps.InformAgreedExtensionDate();
    await DefendantSolicitor1Steps.AcknowledgeClaimFullDefence();
    await DefendantSolicitor1Steps.AddLitigationFriend();
    await DefendantSolicitor1Steps.RespondFastFullDefence1v1();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.RespondFastProceed1v1();
  });
});
