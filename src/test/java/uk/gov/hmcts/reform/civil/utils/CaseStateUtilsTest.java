package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class CaseStateUtilsTest {

    FeatureToggleService featureToggleService = mock(FeatureToggleService.class);

    @Test
    void shouldReturnTrue_whenCarmEnabledSmallClaim1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1ProceedWithClaim(YES)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenCarmEnabledSmallClaim1v1ClaimantNotSettle() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLip(CaseDataLiP.builder()
                             .applicant1SettleClaim(NO)
                             .build())
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenCarmEnabledSmallClaim1v1PartAdmitClaimantRejectRepaymentPlan() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1AcceptAdmitAmountPaidSpec(NO)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenPartAdmitStatesPaidApplicantNotReceivedPayment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1PartAdmitConfirmAmountPaidSpec(NO)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenPartAdmitStatesPaidApplicantRejects() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenCarmEnabledSmallClaim2v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .multiPartyClaimTwoApplicants()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1ProceedWithClaimSpec2v1(YES)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnFalse_whenCarmEnabledSmallClaim1v1ApplicantNotProceed() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .multiPartyClaimTwoApplicants()
            .applicant1ProceedWithClaim(NO)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenCarmEnabledSmallClaim2v1ApplicantNotProceed() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .multiPartyClaimTwoApplicants()
            .applicant1ProceedWithClaimSpec2v1(NO)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenCarmNotEnabledSmallClaim1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1ProceedWithClaim(YES)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenCarmEnabledFastClaim1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(FAST_CLAIM.name())
            .applicant1ProceedWithClaim(YES)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenCarmEnabledFastClaim2v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .multiPartyClaimTwoApplicants()
            .responseClaimTrack(FAST_CLAIM.name())
            .applicant1ProceedWithClaimSpec2v1(YES)
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, featureToggleService);

        assertThat(actual).isFalse();
    }
}
