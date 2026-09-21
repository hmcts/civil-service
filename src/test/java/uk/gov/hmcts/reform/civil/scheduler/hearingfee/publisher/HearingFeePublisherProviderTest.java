package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingFeePublisherProviderTest {

    @Mock
    private MultiOrIntermediateTrackProvider multiOrIntermediateTrackProvider;
    @Mock
    private PreMultiIntermediateClaimProvider preMultiIntermediateClaimProvider;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseData caseData;
    @Mock
    private Consumer<Long> publisher;

    @InjectMocks
    private HearingFeePublisherProvider hearingFeePublisherProvider;

    @Test
    void shouldReturnMultiOrIntermediatePublisher_whenMultiOrIntermediateTrackEnabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);
        when(multiOrIntermediateTrackProvider.getPublisher(caseData)).thenReturn(publisher);

        Consumer<Long> result = hearingFeePublisherProvider.provide(caseData);

        assertThat(result).isEqualTo(publisher);
        verify(multiOrIntermediateTrackProvider).getPublisher(caseData);
    }

    @Test
    void shouldReturnPreMultiIntermediatePublisher_whenMultiOrIntermediateTrackDisabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);
        when(preMultiIntermediateClaimProvider.getPublisher(caseData)).thenReturn(publisher);

        Consumer<Long> result = hearingFeePublisherProvider.provide(caseData);

        assertThat(result).isEqualTo(publisher);
        verify(preMultiIntermediateClaimProvider).getPublisher(caseData);
    }
}
