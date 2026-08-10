import { test } from '../../../playwright-fixtures';

test.describe(
  '1v1 spec request for reconsideration > create a new sdo',
  {
    tag: ['@civil-ccd-nightly'],
  },
  () => {
    test('1v1 spec request for reconsideration > create a new sdo', async ({
      ClaimantSolicitorSpecApiSteps,
      DefendantSolicitor1SpecApiSteps,
      CaseRoleAssignmentApiSteps,
      CaseworkerApiSteps,
      LegalAdvisorApiSteps,
      ClaimantSolicitorSpecSteps,
      JudgeSteps,
    }) => {
      await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
      await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence();
      await ClaimantSolicitorSpecApiSteps.RespondSmallRejectFullDefence();
      await CaseworkerApiSteps.MediationUnsuccessful();
      await LegalAdvisorApiSteps.SdoSmallTrackNoSum();
      await ClaimantSolicitorSpecSteps.Login();
      await ClaimantSolicitorSpecSteps.RequestForReconsideration();
      await JudgeSteps.LoginRegion1();
      await JudgeSteps.DecisionOnReconsiderationRequestCreateSdo();
      await JudgeSteps.SdoSmallTrackNoSum();
    });
  },
);
