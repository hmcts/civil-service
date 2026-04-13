package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CamundaProcessIdentifierTest {

    @Test
    void shouldContainAllExpectedEnumValues() {
        CamundaProcessIdentifier[] values = CamundaProcessIdentifier.values();

        assertThat(values).hasSize(67)
            .contains(
                CamundaProcessIdentifier.ClaimDismissedNotifyParties,
                CamundaProcessIdentifier.ClaimantConfirmProceedNotifyParties,
                CamundaProcessIdentifier.RejectRepaymentPlanNotifyParties,
                CamundaProcessIdentifier.LitigationFriendAddedNotifier,
                CamundaProcessIdentifier.ChangeOfRepresentationNotifyParties,
                CamundaProcessIdentifier.ClaimantLipRepresentedWithNoCNotifier,
                CamundaProcessIdentifier.DefendantLipRepresentedWithNoCNotifier,
                CamundaProcessIdentifier.GenerateDJFormNotifyParties,
                CamundaProcessIdentifier.ClaimProceedsOfflineUnspecNotifyApplicantSolicitor,
                CamundaProcessIdentifier.UnspecNotifyClaimNotifier,
                CamundaProcessIdentifier.UnspecNotifyClaimDetailsNotifier,
                CamundaProcessIdentifier.AcknowledgeClaimUnspecNotifyParties,
                CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder,
                CamundaProcessIdentifier.GenerateOrderNotifyParties,
                CamundaProcessIdentifier.TakenOfflineCaseForSpecNotifier,
                CamundaProcessIdentifier.RaisingClaimAgainstSpecLitigantInPersonForNotifier,
                CamundaProcessIdentifier.ClaimSubmissionNotifyParties,
                CamundaProcessIdentifier.ContinuingClaimOnlineSpecClaimNotifier,
                CamundaProcessIdentifier.MediationSuccessfulNotifyParties,
                CamundaProcessIdentifier.MediationUnsuccessfulNotifyParties,
                CamundaProcessIdentifier.DismissCaseNotifier,
                CamundaProcessIdentifier.HwFOutcomeNotifyParties,
                CamundaProcessIdentifier.DefendantResponseUnspecFullDefenceNotifyParties,
                CamundaProcessIdentifier.DefendantResponseUnspecCaseHandedOfflineNotifyParties,
                CamundaProcessIdentifier.DefendantResponseSpecCaseHandedOfflineNotifyParties,
                CamundaProcessIdentifier.DefendantResponseSpecFullDefenceFullPartAdmitNotifyParties,
                CamundaProcessIdentifier.DefendantResponseSpecOneRespRespondedNotifyParties,
                CamundaProcessIdentifier.DefendantResponseSpecLipvLRFullOrPartAdmit,
                CamundaProcessIdentifier.CreateClaimAfterPaymentContinuingOfflineNotifier,
                CamundaProcessIdentifier.CreateClaimAfterPaymentContinuingOnlineNotifier,
                CamundaProcessIdentifier.JudgmentVariedDeterminationOfMeansNotifyParties,
                CamundaProcessIdentifier.ClaimSubmittedApplicantNotifier,
                CamundaProcessIdentifier.ClaimantLipHelpWithFeesNotifier,
                CamundaProcessIdentifier.DiscontinuanceClaimNotifyParties,
                CamundaProcessIdentifier.ExtendResponseDeadlineNotifier,
                CamundaProcessIdentifier.DefendantResponseCUINotify,
                CamundaProcessIdentifier.TakeCaseOfflineNotifier,
                CamundaProcessIdentifier.UnpaidHearingFeeNotifier,
                CamundaProcessIdentifier.ClaimantResponseConfirmsNotToProceedNotify,
                CamundaProcessIdentifier.ClaimantConfirmsToProceedNotify,
                CamundaProcessIdentifier.ClaimantDefendantAgreedMediationNotify,
                CamundaProcessIdentifier.ClaimantResponseNotAgreedRepaymentNotify,
                CamundaProcessIdentifier.ClaimantResponseAgreedRepaymentNotify,
                CamundaProcessIdentifier.ClaimantResponseAgreedSettledPartAdmitNotify,
                CamundaProcessIdentifier.ClaimantResponseConfirmsNotToProceedLipNotify,
                CamundaProcessIdentifier.HearingNoticeGeneratorNotifier,
                CamundaProcessIdentifier.HearingNoticeGeneratorHMCNotifier,
                CamundaProcessIdentifier.DefendantSignSettlementNotify,
                CamundaProcessIdentifier.CaseProceedsInCasemanNotify,
                CamundaProcessIdentifier.ApplicantNotifyOthersTrialReady,
                CamundaProcessIdentifier.Respondent1NotifyOthersTrialReady,
                CamundaProcessIdentifier.Respondent2NotifyOthersTrialReady,
                CamundaProcessIdentifier.AmendRestitchBundleNotify,
                CamundaProcessIdentifier.BundleCreationNotify,
                CamundaProcessIdentifier.CreateSDONotify,
                CamundaProcessIdentifier.RaiseQueryNotifier,
                CamundaProcessIdentifier.OtherPartyQueryRaisedNotifier,
                CamundaProcessIdentifier.NotifyLipGenericTemplateNotifier,
                CamundaProcessIdentifier.NotifyLipResetPinNotifier,
                CamundaProcessIdentifier.GenerateSpecDJFormNotifier,
                CamundaProcessIdentifier.NotifyDecisionOnReconsiderationRequestNotifier,
                CamundaProcessIdentifier.SettleClaimPaidInFullNotificationNotifier,
                CamundaProcessIdentifier.ClaimantResponsePartAdmitPayImmediatelyNotifier,
                CamundaProcessIdentifier.DJ_NON_DIVERGENT_NOTIFIER
        );
    }

    @Test
    void shouldGetValueByName() {
        CamundaProcessIdentifier identifier = CamundaProcessIdentifier.valueOf("DJ_NON_DIVERGENT_NOTIFIER");

        assertThat(identifier).isEqualTo(CamundaProcessIdentifier.DJ_NON_DIVERGENT_NOTIFIER);
    }

    @Test
    void shouldGetCorrectStringRepresentation() {
        String name = CamundaProcessIdentifier.DJ_NON_DIVERGENT_NOTIFIER.toString();

        assertThat(name).isEqualTo("DJ_NON_DIVERGENT_NOTIFIER");
    }

    @Test
    void shouldThrowExceptionForInvalidEnumName() {
        assertThrows(IllegalArgumentException.class, () ->
            CamundaProcessIdentifier.valueOf("INVALID_ENUM_NAME")
        );
    }

    @Test
    void shouldGetEnumByOrdinal() {
        CamundaProcessIdentifier[] values = CamundaProcessIdentifier.values();

        assertThat(values)
            .satisfies(v -> assertThat(v[0]).isEqualTo(CamundaProcessIdentifier.ClaimDismissedNotifyParties))
            .satisfies(v -> assertThat(v[66]).isEqualTo(CamundaProcessIdentifier.DJ_NON_DIVERGENT_NOTIFIER));
    }
}
