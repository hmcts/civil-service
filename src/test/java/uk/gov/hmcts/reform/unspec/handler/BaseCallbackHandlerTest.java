package uk.gov.hmcts.reform.unspec.handler;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CallbackVersion;

import java.util.Map;

public class BaseCallbackHandlerTest {

    public CallbackParams callbackParamsOf(Map<String, Object> data, CallbackType type) {
        return callbackParamsOf(data, type, null, null);
    }

    public CallbackParams callbackParamsOf(Map<String, Object> data,
                                           CallbackType type,
                                           CallbackVersion version,
                                           Map<CallbackParams.Params, Object> params
    ) {
        return CallbackParams.builder()
            .type(type)
            .request(toCallbackRequest(data))
            .version(version)
            .params(params)
            .build();
    }

    private CallbackRequest toCallbackRequest(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(data).build())
            .build();
    }
}
