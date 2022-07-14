package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.parseLong;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Component
public class UpdateFromGACaseEventTaskHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    private CaseData generalAppCaseData;
    private CaseData civilCaseData;
    private CaseData data;

    @Override
    public void handleTask(ExternalTask externalTask) {
        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String generalAppCaseId = variables.getCaseId();
        String civilCaseId = variables.getGeneralAppParentCaseLink();

        generalAppCaseData = caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(generalAppCaseId)));

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(civilCaseId, variables.getCaseEvent());
        civilCaseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());

        data = coreCaseDataService.submitUpdate(
            civilCaseId,
            CaseDataContentConverter.caseDataContentFromStartEventResponse(
                startEventResponse,
                getUpdatedCaseData(civilCaseData, generalAppCaseData)
            )
        );
    }

    private Map<String, Object> getUpdatedCaseData(CaseData civilCaseData, CaseData generalAppCaseData) {
        List<Element<CaseDocument>> generalOrderDocument =
            ofNullable(civilCaseData.getGeneralOrderDocument()).orElse(newArrayList());
        generalOrderDocument.add(generalAppCaseData.getGeneralOrderDocument() != null ? generalAppCaseData
            .getGeneralOrderDocument().get(0) : null);

        List<Element<CaseDocument>> dismissalOrderDocument =
            ofNullable(civilCaseData.getDismissalOrderDocument()).orElse(newArrayList());
        dismissalOrderDocument.add(generalAppCaseData.getDismissalOrderDocument() != null ? generalAppCaseData
            .getDismissalOrderDocument().get(0) : null);

        List<Element<CaseDocument>> directionOrderDocumnet =
            ofNullable(civilCaseData.getDirectionOrderDocument()).orElse(newArrayList());
        directionOrderDocumnet.add(generalAppCaseData.getDirectionOrderDocument() != null ? generalAppCaseData
            .getDirectionOrderDocument().get(0) : null);

        Map<String, Object> output = civilCaseData.toMap(mapper);
        output.put("generalOrderDocument", generalOrderDocument);
        output.put("dismissalOrderDocument", dismissalOrderDocument);
        output.put("directionOrderDocument", directionOrderDocumnet);

        return output;
    }
}
