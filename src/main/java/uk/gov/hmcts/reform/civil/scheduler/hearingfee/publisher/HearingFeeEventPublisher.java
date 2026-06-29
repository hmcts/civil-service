package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
@AllArgsConstructor
@Slf4j
public class HearingFeeEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public Consumer<Long> createPublisher(String logMessage, Function<Long, Object> eventFactory) {
        return (caseId) -> {
            log.info(logMessage);
            applicationEventPublisher.publishEvent(eventFactory.apply(caseId));
        };
    }
}
