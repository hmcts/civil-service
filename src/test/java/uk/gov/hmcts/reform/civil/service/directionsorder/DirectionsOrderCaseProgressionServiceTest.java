package uk.gov.hmcts.reform.civil.service.directionsorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        when(journeyToggleService.resolveEaCourtLocation(caseData)).thenReturn(YesOrNo.YES);

        service.applyEaCourtLocation(caseData, builder);

        assertThat(builder.build().getEaCourtLocation()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSkipEaCourtLocationWhenResolverReturnsNull() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        when(journeyToggleService.resolveEaCourtLocation(caseData)).thenReturn(null);

        service.applyEaCourtLocation(caseData, builder);

        assertThat(builder.build().getEaCourtLocation()).isNull();
    }

    @Test
    void shouldDelegateWaUpdateWhenFeatureEnabled() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        service.updateWaLocationsIfEnabled(caseData, builder, AUTH);

        verify(locationService).updateWaLocationsIfRequired(caseData, builder, AUTH);
    }

    @Test
    void shouldSkipWaUpdateWhenFeatureDisabled() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        service.updateWaLocationsIfEnabled(caseData, builder, AUTH);

        verify(locationService, never()).updateWaLocationsIfRequired(any(), any(), anyString());
        verify(locationService).clearWaLocationMetadata(builder);
    }

    @Test
    void shouldLeaveWaMetadataWhenFeatureDisabledAndClearFlagFalse() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        service.updateWaLocationsIfEnabled(caseData, builder, AUTH, false);

        verify(locationService, never()).clearWaLocationMetadata(builder);
        verify(locationService, never()).updateWaLocationsIfRequired(any(), any(), anyString());
    }

    @Test
    void shouldApplyRoutingAndUpdateWaMetadata() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        when(journeyToggleService.resolveEaCourtLocation(caseData)).thenReturn(YesOrNo.YES);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        service.applyCaseProgressionRouting(caseData, builder, AUTH);

        verify(locationService).updateWaLocationsIfRequired(caseData, builder, AUTH);
        assertThat(builder.build().getEaCourtLocation()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldClearWaMetadataWhenRoutingDisabled() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        service.applyCaseProgressionRouting(caseData, builder, AUTH);

        verify(locationService).clearWaLocationMetadata(builder);
        verify(locationService, never()).updateWaLocationsIfRequired(any(), any(), anyString());
    }

    @Test
    void shouldKeepWaMetadataWhenRoutingDisabledAndClearFlagFalse() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        service.applyCaseProgressionRouting(caseData, builder, AUTH, false);

        verify(locationService, never()).clearWaLocationMetadata(builder);
        verify(locationService, never()).updateWaLocationsIfRequired(any(), any(), anyString());
    }
}
