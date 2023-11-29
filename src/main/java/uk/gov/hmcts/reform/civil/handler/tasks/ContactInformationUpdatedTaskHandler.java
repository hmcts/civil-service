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
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import static java.util.Optional.ofNullable;

/**
 * Handles the external camunda task for updating contact information.
 * This task handler retrieves caseId and caseEvent from the external task variables and the businessProcess and
 * the contactDetailsUpdatedEvent stored in case data. The contactDetailsUpdatedEvent contains the field changes made
 * when the MANAGE_CONTACT_INFORMATION user event was submitted. This is used to populate the summary and description of
 * when triggering the given caseEvent via core case data service.
 */
@RequiredArgsConstructor
@Component
public class ContactInformationUpdatedTaskHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    @Override
    public void handleTask(ExternalTask externalTask) {
        try {
            ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
            String caseId = ofNullable(variables.getCaseId())
                .orElseThrow(() -> new InvalidCaseDataException("The caseId was not provided"));
            CaseEvent caseEvent = ofNullable(variables.getCaseEvent())
                .orElseThrow(() -> new InvalidCaseDataException("The case event was not provided"));

            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, caseEvent);

            coreCaseDataService.submitUpdate(caseId, caseDataContent(startEventResponse, externalTask));

        } catch (ValueMapperException | IllegalArgumentException e) {
            throw new InvalidCaseDataException("Mapper conversion failed due to incompatible types", e);
        }
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, ExternalTask externalTask) {
        CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = updateProcessInstanceId(caseData.getBusinessProcess(), externalTask);
        ContactDetailsUpdatedEvent event = ofNullable(caseData.getContactDetailsUpdatedEvent())
            .orElseThrow(() -> new InvalidCaseDataException("The contactDetailsUpdatedEvent was not provided"));

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .description(event.getDescription())
                       .summary(event.getSummary())
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
}
