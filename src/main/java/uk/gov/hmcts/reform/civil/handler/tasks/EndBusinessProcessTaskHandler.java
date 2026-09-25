package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.RuntimeService;
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
import uk.gov.hmcts.reform.civil.service.CoreCaseEventDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HEARING_NOTICE;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Slf4j
@Component
public class EndBusinessProcessTaskHandler extends BaseExternalTaskHandler {

    private static final String INVALID_HEARING_NOTICE_PENDING = "invalidHearingNoticePending";

    private final RuntimeService runtimeService;
    private final CoreCaseEventDataService coreCaseEventDataService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    public EndBusinessProcessTaskHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        CoreCaseDataService coreCaseDataService,
        CaseDetailsConverter caseDetailsConverter,
        ObjectMapper mapper,
        RuntimeService runtimeService,
        CoreCaseEventDataService coreCaseEventDataService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.mapper = mapper;
        this.runtimeService = runtimeService;
        this.coreCaseEventDataService = coreCaseEventDataService;
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
        boolean hearingNoticeSkipped = Boolean.TRUE.equals(externalTaskInput.getHearingNoticeSkipped());
        boolean hearingNoticePending = Boolean.TRUE.equals(externalTaskInput.getInvalidHearingNoticePending());
        if (businessProcess.getStatusOrDefault() != BusinessProcessStatus.FINISHED) {
            if (hearingNoticeSkipped) {
                // Persist before ending the business process so event failures can be recovered on retry.
                runtimeService.setVariable(externalTask.getProcessInstanceId(), INVALID_HEARING_NOTICE_PENDING, true);
                hearingNoticePending = true;
            }
            coreCaseDataService.submitUpdate(caseId, caseDataContent(startEventResponse, businessProcess));
        } else {
            log.info("Stopping multiple calls, END_BUSINESS_PROCESS already performed for caseid: {}", caseId);
        }
        if (hearingNoticeSkipped && hearingNoticePending) {
            log.info("Triggering manual hearing listing task for case {} after hearing notice was skipped", caseId);
            String eventDescription = "Hearing notice skipped: " + externalTask.getProcessInstanceId();
            boolean eventAlreadySubmitted = coreCaseEventDataService.getEventsForCase(caseId).stream()
                .anyMatch(event -> INVALID_HEARING_NOTICE.name().equals(event.getId())
                    && eventDescription.equals(event.getDescription()));
            if (!eventAlreadySubmitted) {
                coreCaseDataService.triggerEvent(Long.valueOf(caseId), INVALID_HEARING_NOTICE, Map.of(),
                                                "Invalid hearing notice", eventDescription);
            }
            runtimeService.setVariable(externalTask.getProcessInstanceId(), INVALID_HEARING_NOTICE_PENDING, false);
        }
        return new ExternalTaskData();
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
