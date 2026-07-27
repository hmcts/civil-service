import { test } from '../../../playwright-fixtures/index';

test.describe('1v2 different solicitor unspec full defence api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v2 different solicitor unspec full defence', async ({
    ClaimantSolicitorApiSteps,
    CaseworkerApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
    DefendantSolicitor2ApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast1v2DS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await CaseworkerApiSteps.AddCaseNote();
    await ClaimantSolicitorApiSteps.AmendClaimDocuments();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await CaseworkerApiSteps.AmendPartyDetails();
    await DefendantSolicitor1ApiSteps.AcknowledgeClaimFullDefence();
    await DefendantSolicitor2ApiSteps.AcknowledgeClaimFullDefence();
    await DefendantSolicitor1ApiSteps.InformAgreedExtensionDate();
    await DefendantSolicitor2ApiSteps.InformAgreedExtensionDate();
    await DefendantSolicitor1ApiSteps.RespondFastFullDefence1v2DS();
    await DefendantSolicitor2ApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorApiSteps.RespondFastProceed1v2DS();
  });
});
