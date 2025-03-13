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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.QueryDocumentGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRespondQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getCollectionByMessage;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getLatestQuery;

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

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(builder
                        .businessProcess(BusinessProcess.ready(queryManagementRespondQuery))
                        .build().toMap(mapper))
            .build();

    }

}
