import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 unspec full defence api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v1 unspec full defence', async ({
    ClaimantSolicitorApiSteps,
    CaseworkerApiSteps,
    DefendantSolicitor1ApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast1v1();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await CaseworkerApiSteps.AddCaseNote();
    await ClaimantSolicitorApiSteps.AmendClaimDocuments();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await CaseworkerApiSteps.AmendPartyDetails();
    await DefendantSolicitor1ApiSteps.AcknowledgeClaimFullDefence();
    await DefendantSolicitor1ApiSteps.InformAgreedExtensionDate();
    await DefendantSolicitor1ApiSteps.AddLitigationFriend();
    await DefendantSolicitor1ApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorApiSteps.RespondFastProceed();
  });
});
