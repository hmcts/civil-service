package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDocuments;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.parseLong;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@RequiredArgsConstructor
@Component
public class UpdateFromGACaseEventTaskHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    private CaseData data;
    private CaseData generalAppCaseData;
    private CaseData civilCaseData;

    @Override
    public void handleTask(ExternalTask externalTask) {
        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String generalAppCaseId = variables.getCaseId();
        String civilCaseId = variables.getGeneralAppParentCaseLink();
        List<Element<GeneralApplicationsDocuments>> documents = Collections.emptyList();

        generalAppCaseData = caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(generalAppCaseId)));

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(civilCaseId, variables.getCaseEvent());
        civilCaseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());

        documents = addDocuments(buildDocument(generalAppCaseData), civilCaseData.getGeneralApplicationsDocuments());

        data = coreCaseDataService.submitUpdate(civilCaseId, coreCaseDataService.caseDataContentFromStartEventResponse(
            startEventResponse, getUpdatedCaseData(civilCaseData, documents)));
    }

    private GeneralApplicationsDocuments buildDocument(CaseData generalAppCaseData) {
        return GeneralApplicationsDocuments.builder()
            .generalOrderDocument(generalAppCaseData.getGeneralOrderDocument())
            .dismissalOrderDocument(generalAppCaseData.getDismissalOrderDocument())
            .directionOrderDocument(generalAppCaseData.getDirectionOrderDocument())
            .requestForInformationDocument(generalAppCaseData.getRequestForInformationDocument())
            .hearingOrderDocument(generalAppCaseData.getHearingOrderDocument())
            .writtenRepSequentialDocument(generalAppCaseData.getWrittenRepSequentialDocument())
            .writtenRepConcurrentDocument(generalAppCaseData.getWrittenRepConcurrentDocument())
            .build();
    }

    private List<Element<GeneralApplicationsDocuments>> addDocuments(GeneralApplicationsDocuments document,
                                                                     List<Element<GeneralApplicationsDocuments>>
                                                                         generalApplicationsDocuments) {
        List<Element<GeneralApplicationsDocuments>> newDocument = ofNullable(generalApplicationsDocuments)
            .orElse(newArrayList());
        newDocument.add(element(document));
        return newDocument;
    }

    private Map<String, Object> getUpdatedCaseData(CaseData civilCaseData,
                                                   List<Element<GeneralApplicationsDocuments>> documents) {
        Map<String, Object> output = civilCaseData.toMap(mapper);
        output.put("generalApplicationsDocuments", documents);
        return output;
    }
}
