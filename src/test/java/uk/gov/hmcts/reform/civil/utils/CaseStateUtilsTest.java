package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class CaseStateUtilsTest {

    @Test
    void shouldReturnTrue_whenCarmEnabledSmallClaim1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1ProceedWithClaim(YES)
            .build();

        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, true);

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

        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, true);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenCarmEnabledSmallClaim2v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .multiPartyClaimTwoApplicants()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1ProceedWithClaimSpec2v1(YES)
            .build();

        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, true);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnFalse_whenCarmEnabledSmallClaim1v1ApplicantNotProceed() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .multiPartyClaimTwoApplicants()
            .applicant1ProceedWithClaim(NO)
            .build();

        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenCarmEnabledSmallClaim2v1ApplicantNotProceed() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .multiPartyClaimTwoApplicants()
            .applicant1ProceedWithClaimSpec2v1(NO)
            .build();

        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenCarmNotEnabledSmallClaim1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1ProceedWithClaim(YES)
            .build();

        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, false);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenCarmEnabledFastClaim1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .responseClaimTrack(FAST_CLAIM.name())
            .applicant1ProceedWithClaim(YES)
            .build();

        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenCarmEnabledFastClaim2v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .multiPartyClaimTwoApplicants()
            .responseClaimTrack(FAST_CLAIM.name())
            .applicant1ProceedWithClaimSpec2v1(YES)
            .build();

        boolean actual = CaseStateUtils.shouldMoveToInMediationState(caseData, true);

        assertThat(actual).isFalse();
    }
}
