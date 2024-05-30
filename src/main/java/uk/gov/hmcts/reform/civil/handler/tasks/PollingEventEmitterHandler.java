package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;
import uk.gov.hmcts.reform.civil.service.search.CaseReadyBusinessProcessSearchService;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnExpression("${polling.event.emitter.enabled:true}")
public class PollingEventEmitterHandler implements BaseExternalTaskHandler {

    private final CaseReadyBusinessProcessSearchService caseSearchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final EventEmitterService eventEmitterService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        setupTimeToLive();
        Long multiCasesExecutionDelayInSeconds =
            externalTask.getVariable("multiCasesExecutionDelayInSeconds");
        Set<CaseDetails> cases = Set.copyOf(caseSearchService.getCases());
        log.info("Job '{}' found {} case(s) with IDs {}", externalTask.getTopicName(), cases.size(),
                 cases.stream().map(caseDetails -> caseDetails.getId().toString())
                     .collect(Collectors.joining(","))
        );

        cases.stream()
            .map(caseDetailsConverter::toCaseData)
            .forEach(mappedCase -> {
                log.info(format(
                    "Emitting %s camunda event for case through poller: %d",
                    mappedCase.getBusinessProcess().getCamundaEvent(),
                    mappedCase.getCcdCaseReference()
                ));
                eventEmitterService.emitBusinessProcessCamundaEvent(mappedCase, true);
                delayNextExecution(multiCasesExecutionDelayInSeconds);
            });
    }

    private static void setupTimeToLive() {
        Timer timer = new Timer();
        Thread currentThread = Thread.currentThread();
        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {

                    if (currentThread != null && currentThread.isAlive()) {
                        currentThread.interrupt();
                        timer.cancel();
                    }
                }
            }, TimeUnit.MINUTES.toMillis(55));
    }

    private void delayNextExecution(Long multiCasesExecutionDelayInSeconds) {
        if (multiCasesExecutionDelayInSeconds != null && multiCasesExecutionDelayInSeconds > 0) {
            try {
                TimeUnit.SECONDS.sleep(multiCasesExecutionDelayInSeconds);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
