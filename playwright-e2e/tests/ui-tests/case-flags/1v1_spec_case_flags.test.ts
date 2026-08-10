import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec case flags journey', { tag: '@civil-ccd-nightly' }, 
  async () => {
    test('1v1 spec case flags journey', async ({
      ClaimantSolicitorSpecApiSteps, 
      CaseRoleAssignmentApiSteps,
      DefendantSolicitor1SpecApiSteps,
      HearingCenterAdminSpecSteps
    }) => {
      await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
      await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence();
      await ClaimantSolicitorSpecApiSteps.RespondSmallRejectFullDefence();
      await HearingCenterAdminSpecSteps.LoginRegion1();
      await HearingCenterAdminSpecSteps.CreateCaseLevelCaseFlag();
      await HearingCenterAdminSpecSteps.CreateClaimant1CaseFlag();
    });
});
