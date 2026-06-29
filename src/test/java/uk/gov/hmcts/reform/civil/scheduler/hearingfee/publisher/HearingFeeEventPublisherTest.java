package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HearingFeeEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private HearingFeeEventPublisher hearingFeeEventPublisher;

    @BeforeEach
    void setUp() {
        hearingFeeEventPublisher = new HearingFeeEventPublisher(applicationEventPublisher);
    }

    @Test
    void shouldPublishEvent_whenConsumerIsInvoked() {
        // Given
        Long caseId = 123L;
        String logMessage = "Test log message";
        Object event = new Object();
        Function<Long, Object> eventFactory = id -> event;

        // When
        Consumer<Long> publisher = hearingFeeEventPublisher.createPublisher(logMessage, eventFactory);
        publisher.accept(caseId);

        // Then
        verify(applicationEventPublisher).publishEvent(event);
    }
}
