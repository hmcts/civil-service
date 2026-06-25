package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreMultiIntermediateClaimProviderTest {

    @Mock
    private HearingFeeEventPublisher hearingFeeEventPublisher;
    @Mock
    private PaidEventStrategy paidEventStrategy;
    @Mock
    private UnpaidEventStrategy unpaidEventStrategy;
    @Mock
    private CaseData caseData;
    @Mock
    private Consumer<Long> publisher;

    private PreMultiIntermediateClaimProvider provider;

    @BeforeEach
    void setUp() {
        provider = new PreMultiIntermediateClaimProvider(hearingFeeEventPublisher, paidEventStrategy, unpaidEventStrategy);
    }

    @Test
    void shouldReturnPaidPublisher_whenPaidStrategySupportsCaseData() {
        // Given
        when(paidEventStrategy.supports(caseData)).thenReturn(true);
        when(paidEventStrategy.getEventName()).thenReturn("PaidEvent");
        Function<Long, Object> factory = id -> new Object();
        when(paidEventStrategy.getEventFactory()).thenReturn(factory);
        when(hearingFeeEventPublisher.createPublisher(any(), eq(factory))).thenReturn(publisher);
        when(caseData.getCcdCaseReference()).thenReturn(123456789L);
        when(caseData.getCcdState()).thenReturn(uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS);

        // When
        Consumer<Long> result = provider.getPublisher(caseData);

        // Then
        assertThat(result).isEqualTo(publisher);
        verify(hearingFeeEventPublisher).createPublisher(any(), eq(factory));
    }

    @Test
    void shouldReturnUnpaidPublisher_whenPaidStrategyDoesNotSupportCaseData() {
        // Given
        when(paidEventStrategy.supports(caseData)).thenReturn(false);
        when(unpaidEventStrategy.getEventName()).thenReturn("UnpaidEvent");
        Function<Long, Object> factory = id -> new Object();
        when(unpaidEventStrategy.getEventFactory()).thenReturn(factory);
        when(hearingFeeEventPublisher.createPublisher(any(), eq(factory))).thenReturn(publisher);
        when(caseData.getCcdCaseReference()).thenReturn(123456789L);
        when(caseData.getCcdState()).thenReturn(uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS);

        // When
        Consumer<Long> result = provider.getPublisher(caseData);

        // Then
        assertThat(result).isEqualTo(publisher);
        verify(hearingFeeEventPublisher).createPublisher(any(), eq(factory));
    }
}
