package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.ObligationReason;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.ObligationData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Handles the external camunda task for updating contact information.
 * This task handler retrieves caseId and caseEvent from the external task variables and the businessProcess
 */
@RequiredArgsConstructor
@Component
public class ConfirmOrderReviewTaskHandler extends BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        try {
            ExternalTaskInput taskVariables = parseExternalTaskVariables(externalTask);

            String caseIdentifier = validateCaseId(taskVariables.getCaseId());
            CaseEvent event = validateCaseEvent(taskVariables.getCaseEvent());

            StartEventResponse startEvent = coreCaseDataService.startUpdate(caseIdentifier, event);

            coreCaseDataService.submitUpdate(caseIdentifier, caseDataContent(startEvent, externalTask));

            return ExternalTaskData.builder().build();
        } catch (ValueMapperException | IllegalArgumentException ex) {
            throw new InvalidCaseDataException("Mapper conversion failed due to incompatible types", ex);
        }
    }

    private ExternalTaskInput parseExternalTaskVariables(ExternalTask externalTask) {
        return mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
    }

    private String validateCaseId(String caseId) {
        return ofNullable(caseId)
            .orElseThrow(() -> new InvalidCaseDataException("The caseId was not provided"));
    }

    private CaseEvent validateCaseEvent(CaseEvent caseEvent) {
        return ofNullable(caseEvent)
            .orElseThrow(() -> new InvalidCaseDataException("The case event was not provided"));
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, ExternalTask externalTask) {
        CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = updateProcessInstanceId(caseData.getBusinessProcess(), externalTask);

        String obligationReasons = getObligationReasons(caseData);
        String obligationActions = obligationActions(caseData);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .description(obligationActions)
                       .summary(obligationReasons)
                       .build())
            .data(caseData.toBuilder()
                      .businessProcess(businessProcess)
                      .build().toMap(mapper))
            .build();
    }

    private BusinessProcess updateProcessInstanceId(BusinessProcess businessProcess, ExternalTask externalTask) {
        return businessProcess.hasSameProcessInstanceId(externalTask.getProcessInstanceId())
            ? businessProcess : businessProcess.updateProcessInstanceId(externalTask.getProcessInstanceId());
    }

    private String getObligationReasons(CaseData caseData) {
        return Optional.ofNullable(caseData.getStoredObligationData())
            .orElse(Collections.emptyList())
            .stream()
            .map(element -> {
                ObligationData data = element.getValue();
                ObligationReason reason = data.getObligationReason();

                if (reason == ObligationReason.OTHER) {
                    return reason.getDisplayedValue() + ": " + data.getOtherObligationReason();
                }
                return reason.getDisplayedValue();
            })
            .collect(Collectors.joining(" --- "));
    }

    private String obligationActions(CaseData caseData) {
        return Optional.ofNullable(caseData.getStoredObligationData())
            .orElse(Collections.emptyList())
            .stream()
            .map(element -> element.getValue().getObligationAction())
            .collect(Collectors.joining(" --- "));
    }
}
