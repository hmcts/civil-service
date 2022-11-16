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

import java.util.List;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnExpression("${POLLING_EVENT_EMITTER_ENABLED:true}")
public class PollingEventEmitterHandler implements BaseExternalTaskHandler {

    private final CaseReadyBusinessProcessSearchService caseSearchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final EventEmitterService eventEmitterService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());
        cases.stream()
            .map(caseDetailsConverter::toCaseData)
            .forEach(mappedCase -> {
                log.info(format("Emitting %s camunda event for case through poller: %d",
                                mappedCase.getBusinessProcess().getCamundaEvent(),
                                mappedCase.getCcdCaseReference()));
                eventEmitterService.emitBusinessProcessCamundaEvent(mappedCase, true);
            });
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
