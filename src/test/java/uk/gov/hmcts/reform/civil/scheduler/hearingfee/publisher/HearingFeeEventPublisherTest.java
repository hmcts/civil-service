package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.function.Consumer;
import java.util.function.LongFunction;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HearingFeeEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private HearingFeeEventPublisher hearingFeeEventPublisher;

    @BeforeEach
    void setUp() {
        hearingFeeEventPublisher = new HearingFeeEventPublisher(applicationEventPublisher);
    }

    @Test
    void shouldPublishEvent_whenConsumerIsInvoked() {
        Long caseId = 123L;
        String logMessage = "Test log message";
        Object event = new Object();
        LongFunction<Object> eventFactory = id -> event;

        Consumer<Long> publisher = hearingFeeEventPublisher.createPublisher(logMessage, eventFactory);
        publisher.accept(caseId);

        verify(applicationEventPublisher).publishEvent(event);
    }
}
