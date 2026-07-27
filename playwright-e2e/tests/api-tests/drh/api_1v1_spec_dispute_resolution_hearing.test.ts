import { test } from '../../../playwright-fixtures/index';

test.describe(
  'Dispute resolution hearing API test - spec small claim',
  { tag: '@civil-service-nightly' },
  async () => {
    test('1v1 spec small create SDO for DRH', async ({
      ClaimantSolicitorSpecApiSteps,
      CaseRoleAssignmentApiSteps,
      DefendantSolicitor1SpecApiSteps,
      CaseworkerApiSteps,
      LegalAdvisorApiSteps
    }) => {
      await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
      await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
      await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
      await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence();
      await ClaimantSolicitorSpecApiSteps.RespondSmallRejectFullDefence();
      await CaseworkerApiSteps.MediationUnsuccessful();
      await ClaimantSolicitorSpecApiSteps.UploadMediationDocuments();
      await DefendantSolicitor1SpecApiSteps.UploadMediationDocuments();
      await LegalAdvisorApiSteps.SdoSmallTrackSumDRH();
    });
  },
);
