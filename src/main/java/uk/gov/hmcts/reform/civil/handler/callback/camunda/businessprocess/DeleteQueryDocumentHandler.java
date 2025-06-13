package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_QUERY_DOCUMENT;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class DeleteQueryDocumentHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
            DELETE_QUERY_DOCUMENT
    );

    private final QueryManagementCamundaService camundaService;
    protected final DocumentManagementService documentManagementService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::handleDeleteQueryDocument);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return "DeleteQueryDocument";
    }

    private CallbackResponse handleDeleteQueryDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String userAuthToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String documentId = camundaService.getProcessVariables(
            caseData.getBusinessProcess().getProcessInstanceId()).getDocumentToRemoveId();
        documentManagementService.deleteDocument(userAuthToken, documentId);
        return emptyCallbackResponse(callbackParams);
    }

}
