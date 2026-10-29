package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Slf4j
@Component
public class EndBusinessProcessTaskHandler extends BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    public EndBusinessProcessTaskHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        CoreCaseDataService coreCaseDataService,
        CaseDetailsConverter caseDetailsConverter,
        ObjectMapper mapper
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.mapper = mapper;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        ExternalTaskInput externalTaskInput = mapper.convertValue(
            externalTask.getAllVariables(),
            ExternalTaskInput.class
        );
        String caseId = externalTaskInput.getCaseId();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, END_BUSINESS_PROCESS);
        CaseData data = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = data.getBusinessProcess();
        BusinessProcessStatus status = businessProcess.getStatusOrDefault();
        if (status == BusinessProcessStatus.FINISHED) {
            log.info("Stopping multiple calls, END_BUSINESS_PROCESS already performed for caseid: {}", caseId);
        } else if (isRetryAttempt(externalTask) && status != BusinessProcessStatus.STARTED) {
            log.warn("Skipping END_BUSINESS_PROCESS on retry for caseid: {}, business process status is {} "
                         + "(camundaEvent {}), a newer business process has been triggered since the first attempt",
                     caseId, status, businessProcess.getCamundaEvent());
        } else {
            coreCaseDataService.submitUpdate(caseId, caseDataContent(startEventResponse, businessProcess));
        }
        return new ExternalTaskData();
    }

    private boolean isRetryAttempt(ExternalTask externalTask) {
        return externalTask.getRetries() != null;
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, BusinessProcess businessProcess) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put("businessProcess", businessProcess.reset());

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }

    /**
     * Guarded by the business process status checks in {@code handleTask}. The update is skipped if the
     * status is already {@code FINISHED}. On a retried attempt it is also skipped unless the status is
     * still {@code STARTED}: a {@code READY} or {@code DISPATCHED} status means the first attempt was in
     * fact applied and a newer business process has been triggered since, which must not be reset.
     *
     * <p>The {@code processInstanceId} cannot be compared, because this task runs in the
     * {@code EndBusinessProcess} called process, whose instance id differs from the one stored on the
     * case. Known limitation: if the newer business process has already reached {@code STARTED} by the
     * time of the retry, it is still reset.
     *
     * @return true as this handler checks business process status before submitting the update
     */
    @Override
    protected boolean isRetryGuarded() {
        return true;
    }
}
