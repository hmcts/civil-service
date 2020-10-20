package uk.gov.hmcts.reform.unspec.service.tasks.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.service.search.CaseReadyBusinessProcessSearchService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnExpression("${polling.event.emitter.enabled:true}")
public class PollingEventEmitterHandler implements ExternalTaskHandler {

    private final CaseReadyBusinessProcessSearchService caseSearchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RuntimeService runtimeService;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        final String taskName = externalTask.getTopicName();

        List<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", taskName, cases.size());
        cases.stream().map(caseDetailsConverter::toCaseData).forEach(caseData -> {
            var caseId = caseData.getCcdCaseReference();
            var businessProcess = caseData.getBusinessProcess();
            try {
                var messageCorrelationBuilder = runtimeService
                    .createMessageCorrelation(businessProcess.getCamundaEvent()).setVariable("caseId", caseId);
                messageCorrelationBuilder.correlateStartMessage();
                applicationEventPublisher.publishEvent(new DispatchBusinessProcessEvent(
                    caseId,
                    businessProcess
                ));
            } catch (Exception ex) {
                log.error(String.format("%s task failed for case: %d, message: %s", taskName, caseId, ex.getMessage()));
            }
        });

        externalTaskService.complete(externalTask);
    }
}
