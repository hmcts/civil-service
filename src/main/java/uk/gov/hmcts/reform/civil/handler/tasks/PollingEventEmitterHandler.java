package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
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

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnExpression("${polling.event.emitter.enabled:true}")
public class PollingEventEmitterHandler extends BaseExternalTaskHandler {

    private final CaseReadyBusinessProcessSearchService caseSearchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final EventEmitterService eventEmitterService;
    @Value("${polling.emitter.multiple.cases.delay.seconds:30}")
    private long multiCasesExecutionDelayInSeconds;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = Set.copyOf(caseSearchService.getCases());
        log.info("Job '{}' found {} case(s) with IDs {}", externalTask.getTopicName(), cases.size(),
                 cases.stream().map(caseDetails -> caseDetails.getId().toString())
                     .collect(Collectors.joining(","))
        );

        cases.stream()
            .map(caseDetailsConverter::toCaseData)
            .limit((50 * 60) / multiCasesExecutionDelayInSeconds) // 50 min is the max allowed time to avoid conflicting with next poller execution
            .forEach(mappedCase -> {
                log.info(format(
                    "Emitting %s camunda event for case through poller: %d",
                    mappedCase.getBusinessProcess().getCamundaEvent(),
                    mappedCase.getCcdCaseReference()
                ));
                eventEmitterService.emitBusinessProcessCamundaEvent(mappedCase, true);
                delayNextExecution(multiCasesExecutionDelayInSeconds);
            });
        return ExternalTaskData.builder().build();
    }

    private void delayNextExecution(Long multiCasesExecutionDelayInSeconds) {
        try {
            TimeUnit.SECONDS.sleep(multiCasesExecutionDelayInSeconds);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
