import { test } from '../../../playwright-fixtures/index';

test.describe('1v2DS fast track case progression', { tag: '@civil-ccd-nightly' }, () => {
  test('1v2DS fast track case progression', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1Steps,
    DefendantSolicitor2Steps,
    ClaimantSolicitorSteps,
    JudgeSteps,
    HearingCenterAdminSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast1v2DS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.AmendClaimDocuments();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await DefendantSolicitor1Steps.Login();
    await DefendantSolicitor1Steps.RespondFastFullDefence1v2DS();
    await DefendantSolicitor2Steps.Login();
    await DefendantSolicitor2Steps.RespondFastFullDefence1v2DS();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.RespondFastProceed1v2DS();
    await JudgeSteps.LoginRegion1();
    await JudgeSteps.SdoFast();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.EvidenceUploadFast();
    await DefendantSolicitor1Steps.Login();
    await DefendantSolicitor1Steps.EvidenceUploadFast();
    await HearingCenterAdminSteps.LoginRegion1();
    await HearingCenterAdminSteps.TransferOnlineCase();
  });
});
