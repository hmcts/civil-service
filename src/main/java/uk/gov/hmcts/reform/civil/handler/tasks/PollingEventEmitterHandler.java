package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;
import uk.gov.hmcts.reform.civil.service.search.CaseReadyBusinessProcessSearchService;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.String.format;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

@Slf4j
@Component
@ConditionalOnExpression("${polling.event.emitter.enabled:true}")
public class PollingEventEmitterHandler extends BaseExternalTaskHandler {

    public static final int FIFTY_MINUTES = 3000;
    private final CaseReadyBusinessProcessSearchService caseSearchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final EventEmitterService eventEmitterService;

    public PollingEventEmitterHandler(
        EventProperties eventProperties,
        CaseReadyBusinessProcessSearchService caseSearchService,
        CaseDetailsConverter caseDetailsConverter,
        EventEmitterService eventEmitterService
    ) {
        super(eventProperties);
        this.caseSearchService = caseSearchService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.eventEmitterService = eventEmitterService;
    }

    @Value("${polling.emitter.multiple.cases.delay.seconds:30}")
    private long multiCasesExecutionDelayInSeconds;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = Set.copyOf(caseSearchService.getCases());
        if (log.isInfoEnabled()) {
            log.info("Job '{}' found {} case(s) with IDs {}", externalTask.getTopicName(), cases.size(),
                     cases.stream()
                         .map(caseDetails -> caseDetails.getId().toString()).collect(Collectors.joining(","))
            );
        }
        // 50 min is the max allowed time to avoid conflicting with next poller execution
        long delaySeconds = Math.max(1L, multiCasesExecutionDelayInSeconds);
        long limit = Math.min(cases.size(), (FIFTY_MINUTES / delaySeconds));
        long delayMs = TimeUnit.SECONDS.toMillis(delaySeconds);
        cases.stream()
            .map(caseDetailsConverter::toCaseData)
            .limit(limit)
            .forEach(mappedCase -> {
                log.info(format(
                    "Emitting %s camunda event for case through poller: %d",
                    mappedCase.getBusinessProcess().getCamundaEvent(),
                    mappedCase.getCcdCaseReference()
                ));
                eventEmitterService.emitBusinessProcessCamundaEvent(mappedCase, true);
                throttle(limit, delayMs);
            });
        return new ExternalTaskData();
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
