import { test } from '../../../playwright-fixtures';

test.describe('1v2DS spec settle claim judges order', { tag: ['@civil-ccd-nightly'] }, () => {
  test(`1v2DS spec - settle claim - reason for settlement - following judge's order`, async ({
    ClaimantSolicitorSpecApiSteps,
    DefendantSolicitor1SpecApiSteps,
    DefendantSolicitor2SpecApiSteps,
    CaseRoleAssignmentApiSteps,
    CaseworkerApiSteps,
    LegalAdvisorApiSteps,
    HearingCenterAdminSpecSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v2DS();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence1v2DS();
    await DefendantSolicitor2SpecApiSteps.RespondSmallFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondSmallRejectFullDefence1v2DS();
    await CaseworkerApiSteps.MediationUnsuccessful();
    await LegalAdvisorApiSteps.SdoSmallTrackNoSum();
    await HearingCenterAdminSpecSteps.LoginRegion1();
    await HearingCenterAdminSpecSteps.SettleClaimJudgesOrder();
  });
});
