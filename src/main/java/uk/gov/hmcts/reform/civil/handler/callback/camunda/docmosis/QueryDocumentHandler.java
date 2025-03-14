package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.QueryCollectionType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.QueryDocumentGenerator;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_QUERY_DOCUMENT;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getCollectionByMessage;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getCollectionType;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getQueryById;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getQueryDocumentCategory;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class QueryDocumentHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
            GENERATE_QUERY_DOCUMENT
    );

    private final QueryManagementCamundaService camundaService;
    protected final QueryDocumentGenerator queryDocumentGenerator;
    protected final SecuredDocumentManagementService documentManagementService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::handleGeneratingHearingDocument);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return "GenerateQueryDocument";
    }

    private CallbackResponse handleGeneratingHearingDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        CaseMessage query = getQueryById(caseData,
                camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getQueryId());
        String parentQueryId = nonNull(query.getParentId()) ? query.getParentId() : query.getId();
        CaseQueriesCollection workingCollection = getCollectionByMessage(caseData, query);
        QueryCollectionType collectionType = getCollectionType(workingCollection, caseData);
        DocCategory queryDocumentCategory = getQueryDocumentCategory(collectionType);
        List<Element<CaseMessage>> messageThread = workingCollection.messageThread(parentQueryId);
        String userAuthToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseDocument caseDocument = queryDocumentGenerator.generate(caseData.getCcdCaseReference(), messageThread, userAuthToken, queryDocumentCategory);
        updateQueryDocument(messageThread.get(0).getValue().getCreatedOn(), caseDocument, builder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper)).build();

    }

    private void updateQueryDocument(LocalDateTime timeQueryRaised, CaseDocument newQueryDocument, CaseData.CaseDataBuilder builder) {
        CaseData caseData = builder.build();
        Element<CaseDocument> existingQueryDocument = caseData.getQueryDocuments()
                .stream().filter(doc -> doc.getValue().getCreatedDatetime().equals(timeQueryRaised)).findFirst()
                .orElse(null);

        boolean queryDocumentExists = nonNull(existingQueryDocument);
        if (queryDocumentExists) {
            caseData.getQueryDocuments().remove(existingQueryDocument);
        }

        camundaService.setProcessVariables(caseData.getBusinessProcess().getProcessInstanceId(),
                QueryManagementVariables.builder()
                        .removeDocument(queryDocumentExists)
                        .documentToRemoveId(queryDocumentExists ? getDocumentId(existingQueryDocument.getValue()) : null)
                        .build());

        caseData.getQueryDocuments().add(element(newQueryDocument));
        builder.queryDocuments(caseData.getQueryDocuments());
    }

    private String getDocumentId(CaseDocument document) {
        String documentUrl = document.getDocumentLink().getDocumentUrl();
        return documentUrl.substring(documentUrl.length() - 36);
    }

}
