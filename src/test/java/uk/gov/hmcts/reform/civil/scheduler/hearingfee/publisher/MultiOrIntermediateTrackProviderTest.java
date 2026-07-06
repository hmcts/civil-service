package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy.HearingFeeEventStrategy;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiOrIntermediateTrackProviderTest {

    @Mock
    private HearingFeeEventPublisher hearingFeeEventPublisher;
    @Mock
    private HearingFeeEventStrategy strategy1;
    @Mock
    private HearingFeeEventStrategy strategy2;
    @Mock
    private CaseData caseData;
    @Mock
    private Consumer<Long> publisher;

    private MultiOrIntermediateTrackProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MultiOrIntermediateTrackProvider(hearingFeeEventPublisher, List.of(strategy1, strategy2));
    }

    @Test
    void shouldReturnPublisher_whenStrategySupportsCaseData() {
        // Given
        when(strategy1.supports(caseData)).thenReturn(false);
        when(strategy2.supports(caseData)).thenReturn(true);
        when(strategy2.getEventName()).thenReturn("TestEvent");
        Function<Long, Object> factory = id -> new Object();
        when(strategy2.getEventFactory()).thenReturn(factory);
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
    void shouldThrowException_whenNoStrategySupportsCaseData() {
        // Given
        when(strategy1.supports(caseData)).thenReturn(false);
        when(strategy2.supports(caseData)).thenReturn(false);
        when(caseData.getCcdCaseReference()).thenReturn(123456789L);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> provider.getPublisher(caseData));
        assertThat(exception.getMessage()).contains("Hearing fee payment details are not set for case: 123456789");
    }
}
