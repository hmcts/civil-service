package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoDisposalGuardServiceTest {

    @Mock
    private SdoFeatureToggleService featureToggleService;

    private SdoDisposalGuardService service;

    @BeforeEach
    void setUp() {
        service = new SdoDisposalGuardService(featureToggleService);
    }

    @Test
    void shouldBlockPrePopulateOnlyForDisposalOrdersInJudicialReferral() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setOrderType(OrderType.DISPOSAL);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);
        when(featureToggleService.isMultiOrIntermediateTrackCase(caseData)).thenReturn(true);

        assertThat(service.shouldBlockPrePopulate(caseData)).isTrue();
    }

    @Test
    void shouldNotBlockPrePopulateWhenNotDisposal() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);
        when(featureToggleService.isMultiOrIntermediateTrackCase(caseData)).thenReturn(true);

        assertThat(service.shouldBlockPrePopulate(caseData)).isFalse();
    }

    @Test
    void shouldBlockOrderDetailsWhenDisposalOnMultiTrack() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setOrderType(OrderType.DISPOSAL);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);
        when(featureToggleService.isMultiOrIntermediateTrackCase(caseData)).thenReturn(true);

        assertThat(service.shouldBlockOrderDetails(caseData)).isTrue();
    }

    @Test
    void shouldNotBlockWhenFeatureDisabled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        assertThat(service.shouldBlockPrePopulate(caseData)).isFalse();
        assertThat(service.shouldBlockOrderDetails(caseData)).isFalse();
    }
}
