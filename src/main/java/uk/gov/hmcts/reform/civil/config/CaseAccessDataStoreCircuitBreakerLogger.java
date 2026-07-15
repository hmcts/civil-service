package uk.gov.hmcts.reform.civil.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseAccessDataStoreCircuitBreakerLogger {

    private static final String CIRCUIT_BREAKER_NAME = "caseAccessDataStoreApi";

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    void registerEventListener() {
        circuitBreakerRegistry
            .circuitBreaker(CIRCUIT_BREAKER_NAME)
            .getEventPublisher()
            .onStateTransition(event ->
                                   log.warn(
                                       "Circuit breaker {} transitioned from {} to {}",
                                       event.getCircuitBreakerName(),
                                       event.getStateTransition().getFromState(),
                                       event.getStateTransition().getToState()
                                   )
            );
    }
}
