package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndBusinessProcessTaskHandler extends BaseExternalTaskHandler {

    public static final String NOT_RETRYABLE_MESSAGE = "Stopping multiple calls, END_BUSINESS_PROCESS already performed.";
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

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
        if (businessProcess.getStatusOrDefault() == uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED) {
            throw new NotRetryableException(NOT_RETRYABLE_MESSAGE);
        } else {
            coreCaseDataService.submitUpdate(caseId, caseDataContent(startEventResponse, businessProcess));
        }
        return ExternalTaskData.builder().build();
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
}
