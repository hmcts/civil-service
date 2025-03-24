package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.QueryCollectionType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.QueryDocumentGenerator;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_QUERY_DOCUMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_QUERY_DOCUMENT_TTL;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getCollectionByMessage;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getCollectionType;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getLatestQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getQueryDocumentCategory;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class UpdateQueryDocumentTTLHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
            UPDATE_QUERY_DOCUMENT_TTL
    );

    private final QueryManagementCamundaService camundaService;
    protected final DocumentManagementService documentManagementService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::handleUpdatingQueryDocumentTTL);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return "UpdateQueryDocumentTTL";
    }

    private CallbackResponse handleUpdatingQueryDocumentTTL(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String userAuthToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        UUID documentId = UUID.fromString(
                camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getDocumentToRemoveId());
//        documentManagementService.updateDocumentTimeToLive(documentId, userAuthToken, LocalDateTime.now().minusDays(1));
        documentManagementService.deleteDocument(userAuthToken, documentId);
        return emptyCallbackResponse(callbackParams);
    }

}
