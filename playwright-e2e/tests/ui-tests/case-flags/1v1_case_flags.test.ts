import {test} from '../../../playwright-fixtures/index';

test.describe('1v1 case flags journey', { tag: '@civil-ccd-nightly' }, 
  async () => {
    test('1v1 case flags journey', async ({
      ClaimantSolicitorApiSteps, 
      DefendantSolicitor1ApiSteps,
      ClaimantSolicitorSteps,
      CaseRoleAssignmentApiSteps,
      DefendantSolicitor1Steps,
      HearingCenterAdminSteps,
    }) => {
      await ClaimantSolicitorApiSteps.CreateClaimSmall1v1();
      await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
      await ClaimantSolicitorApiSteps.NotifyClaim();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await ClaimantSolicitorApiSteps.NotifyClaimDetails();
      await DefendantSolicitor1ApiSteps.AddLitigationFriend();
      await DefendantSolicitor1Steps.Login();
      await DefendantSolicitor1Steps.RespondSmallTrackFullDefence1v1();
      await ClaimantSolicitorSteps.Login();
      await ClaimantSolicitorSteps.RespondSmallProceed1v1();
      await HearingCenterAdminSteps.LoginRegion1();
      await HearingCenterAdminSteps.CreateCaseLevelCaseFlag();
      await HearingCenterAdminSteps.CreateClaimant1CaseFlag();
    });
});
