import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 spec small claims mediation api journey', { tag: '@civil-service-nightly' }, async () => {
  test('1v1 claimant and defendant part admit states paid- claimant not received payment - upload mediation documents', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    CaseworkerApiSteps,
    LegalAdvisorApiSteps
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallPartAdmitHasPaid();
    await ClaimantSolicitorSpecApiSteps.RespondSmallRejectPartAdmitPaidConfirmNotPaid();
    await CaseworkerApiSteps.MediationUnsuccessful();
    await ClaimantSolicitorSpecApiSteps.UploadMediationDocuments();
    await DefendantSolicitor1SpecApiSteps.UploadMediationDocuments();
    await LegalAdvisorApiSteps.SdoSmallTrackNoSum();
  });

  test('1v1 claimant and defendant part admit states paid- claimant received payment rejects PA - upload mediation documents', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    CaseworkerApiSteps
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallPartAdmitHasPaid();
    await ClaimantSolicitorSpecApiSteps.RespondSmallRejectPartAdmitPaidConfirmNotPaid();
    await CaseworkerApiSteps.MediationUnsuccessful();
    await ClaimantSolicitorSpecApiSteps.UploadMediationDocuments();
    await DefendantSolicitor1SpecApiSteps.UploadMediationDocuments();
  });

  test('1v1 claimant and defendant part admit reject upload mediation documents', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    CaseworkerApiSteps,
    LegalAdvisorApiSteps
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallPartAdmitImmediately();
    await ClaimantSolicitorSpecApiSteps.RespondMultiRejectPartAdmit();
    await CaseworkerApiSteps.MediationUnsuccessful();
    await ClaimantSolicitorSpecApiSteps.UploadMediationDocuments();
    await DefendantSolicitor1SpecApiSteps.UploadMediationDocuments();
    await LegalAdvisorApiSteps.SdoSmallTrackSum();
  });

  test('1v1 claimant and defendant upload mediation documents', async ({
    ClaimantSolicitorSpecApiSteps,
    CaseRoleAssignmentApiSteps,
    DefendantSolicitor1SpecApiSteps,
    LegalAdvisorApiSteps,
    CaseworkerApiSteps
  }) => {
    await ClaimantSolicitorSpecApiSteps.CreateClaimSmallTrack1v1();
    await ClaimantSolicitorSpecApiSteps.MakePaymentForClaimIssue();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await DefendantSolicitor1SpecApiSteps.RespondSmallFullDefence();
    await ClaimantSolicitorSpecApiSteps.RespondSmallRejectFullDefence();
    await CaseworkerApiSteps.MediationUnsuccessful();
    await ClaimantSolicitorSpecApiSteps.UploadMediationDocuments();
    await DefendantSolicitor1SpecApiSteps.UploadMediationDocuments();
    await LegalAdvisorApiSteps.SdoSmallTrackSum();
  });
});
