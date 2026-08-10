import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec small track claim journey', { tag: '@civil-ccd-nightly' }, async () => {
  test('1v1 spec small track claim journey', async ({
    ClaimantSolicitorSpecSteps,
    DefendantSolicitor1SpecSteps,
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    CaseworkerSteps,
  }) => {
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.CreateClaimSmallTrack1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecSteps.Login();
    await DefendantSolicitor1SpecSteps.RespondSmallTrackFullDefence1v1();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.RespondSmallProceed1v1();
    await CaseworkerSteps.Login();
    await CaseworkerSteps.MediationUnsuccessful();
  });
});
