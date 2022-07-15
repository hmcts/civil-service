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

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.parseLong;

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

        generalAppCaseData = caseDetailsConverter.toGACaseData(coreCaseDataService
                                                                   .getCase(parseLong(generalAppCaseId)));

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
        List<Element<CaseDocument>> generalOrderDocument = newArrayList();
        if (generalAppCaseData.getGeneralOrderDocument() != null) {
            generalOrderDocument.add(generalAppCaseData.getGeneralOrderDocument().get(0));
        }

        List<Element<CaseDocument>> dismissalOrderDocument = newArrayList();
        if (generalAppCaseData.getDismissalOrderDocument() != null) {
            dismissalOrderDocument.add(generalAppCaseData.getDismissalOrderDocument().get(0));
        }

        List<Element<CaseDocument>> directionOrderDocumnet = newArrayList();
        if (generalAppCaseData.getDirectionOrderDocument() != null) {
            directionOrderDocumnet.add(generalAppCaseData.getDirectionOrderDocument().get(0));
        }

        Map<String, Object> output = civilCaseData.toMap(mapper);
        output.put("generalOrderDocument", generalOrderDocument.isEmpty() ? null : generalOrderDocument);
        output.put("dismissalOrderDocument", dismissalOrderDocument.isEmpty() ? null : dismissalOrderDocument);
        output.put("directionOrderDocument", directionOrderDocumnet.isEmpty() ? null : directionOrderDocumnet);

        return output;
    }
}
