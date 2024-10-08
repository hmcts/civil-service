package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.parseLong;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@RequiredArgsConstructor
@Component
public class CoscApplicationAfterPaymentTaskHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;


    @Override
    public void handleTask(ExternalTask externalTask) {
        try {
            ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);

            String civilCaseId =
                ofNullable(variables.getGeneralAppParentCaseLink())
                    .orElseThrow(() -> new InvalidCaseDataException(
                        "General application parent case link not found"));
            CaseEvent caseEvent = ofNullable(variables.getCaseEvent())
                .orElseThrow(() -> new InvalidCaseDataException("The case event was not provided"));

            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(civilCaseId, caseEvent);

            coreCaseDataService.submitUpdate(civilCaseId, caseDataContentFromStartEventResponse(startEventResponse, Map.of()));

        } catch (NumberFormatException ne) {
            throw new InvalidCaseDataException(
                "Conversion to long datatype failed for general application for a case ", ne
            );
        } catch (IllegalArgumentException | ValueMapperException e) {
            throw new InvalidCaseDataException("Mapper conversion failed due to incompatible types", e);
        }
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, ExternalTask externalTask) {
        CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = updateProcessInstanceId(caseData.getBusinessProcess(), externalTask);


        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseData.toBuilder()
                      .businessProcess(businessProcess)
                      .build().toMap(mapper))
            .build();
    }

    private BusinessProcess updateProcessInstanceId(BusinessProcess businessProcess, ExternalTask externalTask) {
        return businessProcess.updateProcessInstanceId(externalTask.getProcessInstanceId());
    }
}
