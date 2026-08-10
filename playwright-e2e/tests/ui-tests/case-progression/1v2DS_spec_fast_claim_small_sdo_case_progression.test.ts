import { test } from '../../../playwright-fixtures/index';

test.describe('1v2 spec fast claim to small sdo case progression', { tag: '@civil-ccd-nightly' }, () => {
  test('1v2 spec fast claim to small sdo case progression', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecSteps,
    DefendantSolicitor2SpecSteps,
    JudgeSteps,
    ClaimantSolicitorSpecSteps,
    HearingCenterAdminSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimFast1v2DS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.RespondFastFullDefence1v2DS();
    await DefendantSolicitor2SpecSteps.Login();
    await DefendantSolicitor2SpecSteps.RespondFastFullDefence1v2DS();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.RespondFastProceed1v2DS();
    await JudgeSteps.LoginRegion1();
    await JudgeSteps.SdoSmallTrackFromFastClaim();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.EvidenceUploadSmall();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.EvidenceUploadSmall();
    await HearingCenterAdminSteps.LoginRegion1();
    await HearingCenterAdminSteps.ScheduleHearingSmall();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForHearingFee();
  });
});
