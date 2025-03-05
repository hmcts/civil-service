package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.QueryDocumentGenerator;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRespondQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getCollectionByMessage;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getLatestQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getQueryById;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class RespondQueryCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(queryManagementRespondQuery);

    private final ObjectMapper mapper;
    private final AssignCategoryId assignCategoryId;
    private final CoreCaseUserService coreCaseUserService;
    protected final QueryDocumentGenerator queryDocumentGenerator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::setManagementQuery,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse setManagementQuery(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseMessage latestCaseMessage = getLatestQuery(caseData);
        String parentQueryId = latestCaseMessage.getParentId();
        assignCategoryIdToCaseworkerAttachments(caseData, latestCaseMessage, assignCategoryId,
                                                coreCaseUserService, parentQueryId);
        CaseData.CaseDataBuilder<?,?> builder = caseData.toBuilder();
        latestCaseMessage.getAttachments();
        CaseQueriesCollection currentCollection = getCollectionByMessage(caseData, latestCaseMessage);
        List<Element<CaseMessage>> messageThread = currentCollection.messageThread(parentQueryId);
        String categoryId = getQueriesDocument(caseData, messageThread.get(0).getValue().getCreatedOn()).getDocumentLink().getCategoryID();
        buildDocument(messageThread, callbackParams.getParams().get(BEARER_TOKEN).toString(), categoryId, builder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(mapper))
            .build();

    }

    CaseDocument getQueriesDocument(CaseData caseData, LocalDateTime initialQueryTime) {
        return caseData.getQueryDocuments().stream().filter(doc -> doc.getValue().getCreatedDatetime()
            .equals(initialQueryTime)).findFirst()
            .map(doc -> doc.getValue())
            .orElse(CaseDocument.builder().build());
    }

    private void buildDocument(List<Element<CaseMessage>> messageThread, String auth, String categoryId, CaseData.CaseDataBuilder builder) {
        List<CaseDocument> caseDocuments = queryDocumentGenerator.generate(messageThread, auth, categoryId);
        CaseData caseData = builder.build();
        List<Element<CaseDocument>> queryDocuments = new ArrayList<>();
        queryDocuments.add(element(caseDocuments.get(0)));
        if (!isEmpty(caseData.getQueryDocuments())) {
            queryDocuments.addAll(caseData.getHearingDocuments());
        }
        builder.queryDocuments(queryDocuments);
    }
}
