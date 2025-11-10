package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SdoFeatureToggleServiceTest {

    @Mock
    private FeatureToggleService featureToggleService;

    private SdoFeatureToggleService sdoFeatureToggleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sdoFeatureToggleService = new SdoFeatureToggleService(featureToggleService);
    }

    @Test
    void shouldReturnTrue_whenWelshJourneyEnabledForBilingualParties() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference(Language.WELSH.name())
            .build();

        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        assertThat(sdoFeatureToggleService.isWelshJourneyEnabled(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenWelshToggleDisabled() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference(Language.WELSH.name())
            .build();

        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        assertThat(sdoFeatureToggleService.isWelshJourneyEnabled(caseData)).isFalse();
    }

    @Test
    void shouldReturnTrue_whenCaseIsIntermediateTrackAndToggleOn() {
        CaseData caseData = CaseData.builder()
            .allocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM)
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        assertThat(sdoFeatureToggleService.isMultiOrIntermediateTrackCase(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenResponseTrackIsMultiTrackAndToggleOn() {
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(AllocatedTrack.MULTI_CLAIM.name())
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        assertThat(sdoFeatureToggleService.isMultiOrIntermediateTrackCase(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenToggleDisabled() {
        CaseData caseData = CaseData.builder()
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        assertThat(sdoFeatureToggleService.isMultiOrIntermediateTrackCase(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenCaseIsFastTrackEvenIfToggleOn() {
        CaseData caseData = CaseData.builder()
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        assertThat(sdoFeatureToggleService.isMultiOrIntermediateTrackCase(caseData)).isFalse();
    }
}
