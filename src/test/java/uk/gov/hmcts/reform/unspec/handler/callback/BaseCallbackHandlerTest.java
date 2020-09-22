package uk.gov.hmcts.reform.unspec.handler.callback;

import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams.Params;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CallbackVersion;
import uk.gov.hmcts.reform.unspec.enums.CaseState;
import uk.gov.hmcts.reform.unspec.service.UserService;

import java.util.Map;

import static uk.gov.hmcts.reform.unspec.enums.CaseState.CREATED;

public abstract class BaseCallbackHandlerTest {

    public static final Long CASE_ID = 1594901956117591L;
    @MockBean
    protected AuthTokenGenerator authTokenGenerator;
    @MockBean
    protected UserService userService;

    public CallbackParams callbackParamsOf(Map<String, Object> data, CallbackType type, CaseState state) {
        return callbackParamsOf(data, state, type, null, Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN"));
    }

    public CallbackParams callbackParamsOf(Map<String, Object> data, CallbackType type) {
        return callbackParamsOf(data, CREATED, type, null, Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN"));
    }

    public CallbackParams callbackParamsOf(Map<String, Object> data,
                                           CaseState state,
                                           CallbackType type,
                                           CallbackVersion version,
                                           Map<Params, Object> params
    ) {
        return CallbackParams.builder()
            .type(type)
            .request(toCallbackRequest(data, state.name()))
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
