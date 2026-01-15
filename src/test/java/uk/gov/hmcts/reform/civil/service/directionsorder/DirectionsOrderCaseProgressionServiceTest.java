package uk.gov.hmcts.reform.civil.service.directionsorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoJourneyToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectionsOrderCaseProgressionServiceTest {

    private static final String AUTH = "auth-token";

    @Mock
    private SdoJourneyToggleService journeyToggleService;
    @Mock
    private SdoFeatureToggleService featureToggleService;
    @Mock
    private SdoLocationService locationService;

    private DirectionsOrderCaseProgressionService service;

    @BeforeEach
    void setUp() {
        service = new DirectionsOrderCaseProgressionService(journeyToggleService, featureToggleService, locationService);
    }

    @Test
    void shouldApplyEaCourtLocationWhenResolverReturnsValue() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(journeyToggleService.resolveEaCourtLocation(caseData, true)).thenReturn(YesOrNo.YES);

        service.applyEaCourtLocation(caseData, true);

        assertThat(caseData.getEaCourtLocation()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSkipEaCourtLocationWhenResolverReturnsNull() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(journeyToggleService.resolveEaCourtLocation(caseData, true)).thenReturn(null);

        service.applyEaCourtLocation(caseData, true);

        assertThat(caseData.getEaCourtLocation()).isNull();
    }

    @Test
    void shouldDelegateWaUpdateWhenFeatureEnabled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        service.updateWaLocationsIfEnabled(caseData, AUTH);

        verify(locationService).updateWaLocationsIfRequired(caseData, AUTH);
    }

    @Test
    void shouldSkipWaUpdateWhenFeatureDisabledAndClearDefaultFalse() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        service.updateWaLocationsIfEnabled(caseData, AUTH);

        verify(locationService, never()).updateWaLocationsIfRequired(any(), anyString());
        verify(locationService, never()).clearWaLocationMetadata(caseData);
    }

    @Test
    void shouldClearWaMetadataWhenFeatureDisabledAndClearFlagTrue() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        service.updateWaLocationsIfEnabled(caseData, AUTH, true);

        verify(locationService).clearWaLocationMetadata(caseData);
        verify(locationService, never()).updateWaLocationsIfRequired(any(), anyString());
    }

    @Test
    void shouldApplyRoutingAndUpdateWaMetadata() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(journeyToggleService.resolveEaCourtLocation(caseData, true)).thenReturn(YesOrNo.YES);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        service.applyCaseProgressionRouting(caseData, AUTH, true);

        verify(locationService).updateWaLocationsIfRequired(caseData, AUTH);
        assertThat(caseData.getEaCourtLocation()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldClearWaMetadataWhenRoutingDisabled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        service.applyCaseProgressionRouting(caseData, AUTH, true);

        verify(locationService, never()).clearWaLocationMetadata(caseData);
        verify(locationService, never()).updateWaLocationsIfRequired(any(), anyString());
    }

    @Test
    void shouldClearWaMetadataWhenRoutingDisabledAndClearFlagTrueExplicit() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        service.applyCaseProgressionRouting(caseData, AUTH, true, true);

        verify(locationService).clearWaLocationMetadata(caseData);
        verify(locationService, never()).updateWaLocationsIfRequired(any(), anyString());
    }
}
