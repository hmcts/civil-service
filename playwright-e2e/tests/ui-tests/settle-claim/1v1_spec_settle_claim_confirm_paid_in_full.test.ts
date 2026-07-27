import { test } from '../../../playwright-fixtures';

test.describe('1v1 spec settle claim confirm paid in full', { tag: ['@civil-ccd-nightly'] }, () => {
  test('1v1 spec - settle claim - confirm paid in full', async ({
    ClaimantSolicitorSpecApiSteps,
    DefendantSolicitor1SpecApiSteps,
    CaseRoleAssignmentApiSteps,
    CaseworkerApiSteps,
    LegalAdvisorApiSteps,
    ClaimantSolicitorSpecSteps,
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondSmallRejectFullDefence();
    await CaseworkerApiSteps.MediationUnsuccessful();
    await LegalAdvisorApiSteps.SdoSmallTrackNoSum();
    await ClaimantSolicitorSpecSteps.Login();
    await ClaimantSolicitorSpecSteps.SettleClaimMarkPaidInFull();
  });
});
