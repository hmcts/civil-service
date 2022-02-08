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
        List<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());
        cases.stream()
            .peek(caseDetails -> log.info("PolledcaseId ({})", caseDetails.getId()))
            .map(caseDetailsConverter::toCaseData)
            .peek(caseData -> log.info("PolledcaseInfo ({}) businessproc ({}) ({}) ({})",
                caseData.getLegacyCaseReference(), caseData.getBusinessProcess().getStatus(),
                caseData.getBusinessProcess().getCamundaEvent(), caseData.getBusinessProcess().getActivityId()))
            .forEach(mappedCase -> eventEmitterService.emitBusinessProcessCamundaEvent(mappedCase, true));
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
