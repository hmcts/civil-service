package uk.gov.hmcts.reform.civil.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseAccessDataStoreCircuitBreakerLoggerTest {

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @InjectMocks
    private CaseAccessDataStoreCircuitBreakerLogger logger;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        Logger loggerInstance = (Logger) LoggerFactory.getLogger(CaseAccessDataStoreCircuitBreakerLogger.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        loggerInstance.addAppender(listAppender);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldLogStateTransition() {
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);
        CircuitBreaker.EventPublisher eventPublisher = mock(CircuitBreaker.EventPublisher.class);

        when(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi")).thenReturn(circuitBreaker);
        when(circuitBreaker.getEventPublisher()).thenReturn(eventPublisher);

        // This registers the listener
        logger.registerEventListener();

        // Capture the consumer passed to onStateTransition
        ArgumentCaptor<io.github.resilience4j.core.EventConsumer<CircuitBreakerOnStateTransitionEvent>> eventConsumerCaptor =
            ArgumentCaptor.forClass(io.github.resilience4j.core.EventConsumer.class);
        verify(eventPublisher).onStateTransition(eventConsumerCaptor.capture());

        // Create a transition event
        CircuitBreakerOnStateTransitionEvent event = new CircuitBreakerOnStateTransitionEvent(
            "caseAccessDataStoreApi",
            CircuitBreaker.StateTransition.CLOSED_TO_OPEN
        );

        // Invoke the consumer
        eventConsumerCaptor.getValue().consumeEvent(event);

        // Verify log
        assertThat(listAppender.list)
            .extracting(ILoggingEvent::getFormattedMessage)
            .containsExactly("Circuit breaker caseAccessDataStoreApi transitioned from CLOSED to OPEN");
    }
}
