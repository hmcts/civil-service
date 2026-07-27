import { test } from '../../../playwright-fixtures/index';

test.describe('1v2DS stay case journey', { tag: '@civil-ccd-nightly' }, () => {
  test('1v2DS stay case journey', async ({
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1ApiSteps,
    DefendantSolicitor2ApiSteps,
    DefendantSolicitor2Steps,
    HearingCenterAdminSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimFast1v2DS();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS2();
    await ClaimantSolicitorApiSteps.NotifyClaim();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails();
    await DefendantSolicitor1ApiSteps.RespondFastFullDefence1v2DS();
    await DefendantSolicitor2ApiSteps.RespondFastFullDefence();
    await ClaimantSolicitorApiSteps.RespondFastProceed1v2DS();
    await DefendantSolicitor2Steps.Login();
    await DefendantSolicitor2Steps.AddUnavailableDates();
    await HearingCenterAdminSteps.LoginRegion1();
    await HearingCenterAdminSteps.StayCase();
    await HearingCenterAdminSteps.ManageStayRequestUpdate();
    await HearingCenterAdminSteps.ManageStayLiftStay();
  });
});
