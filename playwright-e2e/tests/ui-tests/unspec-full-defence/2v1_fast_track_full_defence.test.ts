import { test } from '../../../playwright-fixtures/index';

test.describe('2v1 fast track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('2v1 fast track claim journey', async ({
    ClaimantSolicitorSteps,
    DefendantSolicitor1Steps,
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.CreateClaimFast2v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorSteps.NotifyClaim();
    await ClaimantSolicitorSteps.NotifyClaimDetails();
    await DefendantSolicitor1Steps.Login();
    await DefendantSolicitor1Steps.AcknowledgeClaimFullDefence2v1();
    await DefendantSolicitor1Steps.InformAgreedExtensionDate();
    await DefendantSolicitor1Steps.RespondFastFullDefence2v1();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.RespondFastProceed2v1();
  });
});
