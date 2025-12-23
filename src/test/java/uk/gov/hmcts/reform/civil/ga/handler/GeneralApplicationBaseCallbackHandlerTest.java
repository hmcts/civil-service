package uk.gov.hmcts.reform.civil.ga.handler;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackParams.Params;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;

public abstract class GeneralApplicationBaseCallbackHandlerTest {

    public static final Long CASE_ID = 1594901956117591L;
    @MockitoBean
    protected AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    protected UserService userService;

    public CallbackParams callbackParamsOf(Map<String, Object> data, CallbackType type, CaseState state) {
        return callbackParamsOf(data, state, type, null, null, Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN"));
    }

    public CallbackParams callbackParamsOf(Map<String, Object> data, CallbackType type) {
        return callbackParamsOf(
            data,
            AWAITING_RESPONDENT_RESPONSE,
            type,
            null,
            null,
            Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN")
        );
    }

    public CallbackParams callbackParamsOf(Map<String, Object> data, CallbackType type, String pageId) {
        return callbackParamsOf(
            data,
            AWAITING_RESPONDENT_RESPONSE,
            type,
            null,
            pageId,
            Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN")
        );
    }

    public CallbackParams callbackParamsOf(GeneralApplicationCaseData caseData, CallbackType type) {
        return callbackParamsOf(caseData, type, null, null, Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN"));
    }

    public CallbackParams callbackParamsOf(CallbackVersion version, GeneralApplicationCaseData caseData, CallbackType type) {
        return callbackParamsOf(caseData, type, version, null, Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN"));
    }

    public CallbackParams callbackParamsOf(GeneralApplicationCaseData caseData, CallbackType type, String pageId) {
        return callbackParamsOf(caseData, type, null, pageId, Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN"));
    }

    public CallbackParams callbackParamsOf(CallbackVersion version,
                                           GeneralApplicationCaseData caseData,
                                           CallbackType type,
                                           String pageId) {
        return callbackParamsOf(caseData, type, version, pageId, Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN"));
    }

    public CallbackParams callbackParamsOf(GeneralApplicationCaseData caseData, CallbackType type, String pageId, String eventId) {
        return specCallbackParamsOf(
            caseData, type, null, pageId, eventId, Map.of(
                Params.BEARER_TOKEN,
                "BEARER_TOKEN"
            )
        );
    }

    public CallbackParams callbackParamsOf(GeneralApplicationCaseData caseData, CaseEvent event, CallbackType type) {
        return callbackParamsOf(caseData, type, null, event.name());
    }

    public CallbackParams callbackParamsOf(GeneralApplicationCaseData caseData,
                                           CallbackType type,
                                           CallbackVersion version,
                                           String pageId,
                                           Map<Params, Object> params
    ) {
        return CallbackParams.builder()
            .type(type)
            .pageId(pageId)
            .request(CallbackRequest.builder()
                         .caseDetails(CaseDetails.builder().data(new HashMap<>()).id(CASE_ID).build())
                         .build())
            .caseData(caseData)
            .version(version)
            .params(params)
            .build();
    }

    public CallbackParams callbackParamsOf(Map<String, Object> data,
                                           CaseState state,
                                           CallbackType type,
                                           CallbackVersion version,
                                           String pageId,
                                           Map<Params, Object> params
    ) {
        return CallbackParams.builder()
            .type(type)
            .pageId(pageId)
            .request(toCallbackRequest(data, state.name()))
            .version(version)
            .params(params)
            .build();
    }

    public CallbackParams callbackParamsOfPendingState(Map<String, Object> data, CallbackType type) {
        return callbackParamsOf(
            data,
            PENDING_APPLICATION_ISSUED,
            type,
            null,
            null,
            Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN")
        );
    }

    public CallbackParams specCallbackParamsOf(GeneralApplicationCaseData caseData,
                                               CallbackType type,
                                               CallbackVersion version,
                                               String pageId,
                                               String eventId,
                                               Map<Params, Object> params
    ) {
        return CallbackParams.builder()
            .type(type)
            .pageId(pageId)
            .request(CallbackRequest.builder()
                         .caseDetails(CaseDetails.builder().data(new HashMap<>()).id(CASE_ID).build())
                         .eventId(eventId)
                         .build())
            .caseData(caseData)
            .version(version)
            .params(params)
            .build();
    }

    private CallbackRequest toCallbackRequest(Map<String, Object> data, String state) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(data).id(CASE_ID).state(state).build())
            .build();
    }
}
