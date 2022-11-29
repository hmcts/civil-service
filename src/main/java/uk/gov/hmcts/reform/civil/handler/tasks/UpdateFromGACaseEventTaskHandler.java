package uk.gov.hmcts.reform.civil.handler.tasks;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.exceptions.CaseIdNotProvidedException;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter;

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
        try {
            ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);

            String generalAppCaseId =
                ofNullable(variables.getCaseId()).orElseThrow(CaseIdNotProvidedException::new);
            String civilCaseId =
                ofNullable(variables.getGeneralAppParentCaseLink())
                    .orElseThrow(() -> new InvalidCaseDataException(
                        "General application parent case link not found"));

            generalAppCaseData = caseDetailsConverter.toGACaseData(coreCaseDataService
                                                                       .getCase(parseLong(generalAppCaseId)));

            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
                civilCaseId,
                variables.getCaseEvent()
            );
            civilCaseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());

            data = coreCaseDataService.submitUpdate(
                civilCaseId,
                CaseDataContentConverter.caseDataContentFromStartEventResponse(
                    startEventResponse,
                    getUpdatedCaseData(civilCaseData, generalAppCaseData)
                )
            );
        } catch (NumberFormatException ne) {
            throw new InvalidCaseDataException(
                "Conversion to long datatype failed for general application for a case ", ne
            );
        } catch (IllegalArgumentException | ValueMapperException e) {
            throw new InvalidCaseDataException("mapper conversion failed due to incompatible types", e);
        }
    }

    private Map<String, Object> getUpdatedCaseData(CaseData civilCaseData, CaseData generalAppCaseData) {
        List<Element<CaseDocument>> generalOrderDocument = ofNullable(civilCaseData.getGeneralOrderDocument())
            .orElse(newArrayList());

        if (generalAppCaseData.getGeneralOrderDocument() != null
            && checkIfDocumentExists(generalOrderDocument, generalAppCaseData.getGeneralOrderDocument()) < 1) {
            generalOrderDocument.addAll(generalAppCaseData.getGeneralOrderDocument());
        }

        List<Element<CaseDocument>> dismissalOrderDocument = ofNullable(civilCaseData.getDismissalOrderDocument())
            .orElse(newArrayList());

        if (generalAppCaseData.getDismissalOrderDocument() != null
            && checkIfDocumentExists(dismissalOrderDocument, generalAppCaseData.getDismissalOrderDocument()) < 1) {
            dismissalOrderDocument.addAll(generalAppCaseData.getDismissalOrderDocument());
        }

        List<Element<CaseDocument>> directionOrderDocument = ofNullable(civilCaseData.getDirectionOrderDocument())
            .orElse(newArrayList());

        if (generalAppCaseData.getDirectionOrderDocument() != null
            && checkIfDocumentExists(directionOrderDocument, generalAppCaseData.getDirectionOrderDocument()) < 1) {
            directionOrderDocument.addAll(generalAppCaseData.getDirectionOrderDocument());
        }

        Map<String, Object> output = civilCaseData.toMap(mapper);
        output.put("generalOrderDocument", generalOrderDocument.isEmpty() ? null : generalOrderDocument);
        output.put("dismissalOrderDocument", dismissalOrderDocument.isEmpty() ? null : dismissalOrderDocument);
        output.put("directionOrderDocument", directionOrderDocument.isEmpty() ? null : directionOrderDocument);

        return output;
    }

    private int checkIfDocumentExists(List<Element<CaseDocument>> civilCaseDocumentList,
                                      List<Element<CaseDocument>> gaCaseDocumentlist) {
        return civilCaseDocumentList.stream().filter(civilDocument -> gaCaseDocumentlist
              .parallelStream().anyMatch(gaDocument -> gaDocument.getId().equals(civilDocument.getId())))
            .collect(Collectors.toList()).size();
    }
}
