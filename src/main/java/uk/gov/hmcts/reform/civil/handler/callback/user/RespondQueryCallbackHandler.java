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
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRespondQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.clearOldQueryCollections;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getLatestQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.migrateAllQueries;

@Service
@RequiredArgsConstructor
public class RespondQueryCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(queryManagementRespondQuery);

    private final ObjectMapper mapper;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::aboutToStart,
            callbackKey(ABOUT_TO_SUBMIT), this::setManagementQuery,
            callbackKey(SUBMITTED), this::emptyCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        if (featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            migrateAllQueries(caseDataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(mapper)).build();
    }

    private CallbackResponse setManagementQuery(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        CaseMessage latestCaseMessage = getLatestQuery(caseData);

        if (!featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            assignCategoryIdToCaseworkerAttachments(caseData, latestCaseMessage, assignCategoryId);
        } else {
            clearOldQueryCollections(caseDataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder
                      .businessProcess(BusinessProcess.ready(queryManagementRespondQuery))
                      .build().toMap(mapper))
            .build();
    }
}
