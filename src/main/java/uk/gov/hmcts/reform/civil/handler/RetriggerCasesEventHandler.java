package uk.gov.hmcts.reform.civil.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Long.parseLong;

@Slf4j
@RequiredArgsConstructor
@Component
public class RetriggerCasesEventHandler extends BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        assert externalTask.getVariable("caseEvent") != null;
        assert externalTask.getVariable("caseIds") != null;

        String caseIds = externalTask.getVariable("caseIds");
        CaseEvent caseEvent = CaseEvent.valueOf(externalTask.getVariable("caseEvent"));
        String eventSummary = "Re-trigger of " + caseEvent.name();
        String eventDescription = "Process ID: %s".formatted(externalTask.getProcessInstanceId());
        Map<String, Object> caseData = getCaseData(externalTask);
        String documentJson = externalTask.getVariable("document");
        String ga = externalTask.getVariable("ga");
        boolean isGaCase = "Yes".equalsIgnoreCase(ga);
        for (String caseId : caseIds.split(",")) {
            if (documentJson != null) {
                handleDocumentUpdateFinalOrders(caseId, caseEvent, documentJson, externalTask.getProcessInstanceId());
                break;
            }
            try {
                externalTask.getAllVariables().put("caseId", caseId);
                if (isGaCase) {
                    log.info("Retrigger GA CaseId: {} started", caseId);
                    coreCaseDataService.triggerGeneralApplicationEvent(parseLong(caseId.trim()),
                                                                       caseEvent);
                    log.info("Retrigger GA CaseId: {} finished. Case data: {}", caseId, caseData);
                } else {
                    log.info("Retrigger CaseId: {} started", caseId);
                    coreCaseDataService.triggerEvent(
                        parseLong(caseId.trim()),
                        caseEvent,
                        caseData,
                        eventSummary,
                        eventDescription
                    );
                    log.info("Retrigger CaseId: {} finished. Case data: {}", caseId, caseData);
                }
            } catch (Exception e) {
                log.error("ERROR Retrigger CaseId: {}. Case data: {}, isGaCase {}, {}", caseId, caseData, isGaCase, e.getMessage(), e);
            }
        }
        return ExternalTaskData.builder().build();
    }

    private Map<String, Object> getCaseData(ExternalTask externalTask) {
        var typeRef = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        String caseDataString = externalTask.getVariable("caseData");

        if (caseDataString == null || caseDataString.isBlank()) {
            return Map.of();
        }

        try {
            return mapper.readValue(caseDataString, typeRef);
        } catch (Exception e) {
            log.error("Case data could not be deserialized {}", caseDataString, e);
            throw new IllegalArgumentException("Exception deserializing case data", e);
        }
    }

    private void handleDocumentUpdateFinalOrders(String caseId,
                                                 CaseEvent caseEvent,
                                                 String documentJson,
                                                 String processInstanceId) {
        CaseDocument document = null;

        try {
            document = mapper.readValue(documentJson, CaseDocument.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Adding Final order document to case %s with error %s", caseId, e));
        }

        CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(caseId));
        Map<String, Object> data = caseDetails.getData();

        JavaType listType = mapper.getTypeFactory()
            .constructCollectionType(List.class,
                                     mapper.getTypeFactory().constructParametricType(Element.class, CaseDocument.class));

        List<Element<CaseDocument>> finalOrderDocumentCollection = data.get("finalOrderDocumentCollection") != null
            ? mapper.convertValue(data.get("finalOrderDocumentCollection"), listType)
            : new ArrayList<>();

        finalOrderDocumentCollection.add(Element.<CaseDocument>builder().id(UUID.randomUUID()).value(document).build());

        Map<String, Object> updatedData = new HashMap<>();

        updatedData.put("finalOrderDocumentCollection", finalOrderDocumentCollection);

        String eventSummary = "Re-trigger of " + caseEvent.name();
        String eventDescription = "Process ID: %s".formatted(processInstanceId);

        coreCaseDataService.triggerEvent(
            parseLong(caseId.trim()),
            caseEvent,
            updatedData,
            eventSummary,
            eventDescription
        );
    }
}
