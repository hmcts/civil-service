package uk.gov.hmcts.reform.unspec.sampledata;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CallbackVersion;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.Map;

public class CallbackParamsBuilder {

    private CallbackType type;
    private CallbackRequest request;
    private Map<CallbackParams.Params, Object> params;
    private CallbackVersion version;
    private String pageId;
    private CaseData caseData;

    public static CallbackParamsBuilder builder() {
        return new CallbackParamsBuilder();
    }

    public CallbackParamsBuilder of(CallbackType type, CaseData caseData) {
        this.type = type;
        this.caseData = caseData;
        this.request = CallbackRequest.builder()
            .caseDetails(CaseDetailsBuilder.builder()
                             .data(caseData)
                             .build())
            .build();
        this.params = Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN");
        return this;
    }

    public CallbackParamsBuilder of(CallbackType type, CaseDetails caseDetails) {
        this.type = type;
        this.request = CallbackRequest.builder().caseDetails(caseDetails).build();
        this.params = Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN");
        return this;
    }

    public CallbackParamsBuilder type(CallbackType type) {
        this.type = type;
        return this;
    }

    public CallbackParamsBuilder request(CallbackRequest request) {
        this.request = request;
        return this;
    }

    public CallbackParamsBuilder version(CallbackVersion version) {
        this.version = version;
        return this;
    }

    public CallbackParamsBuilder pageId(String pageId) {
        this.pageId = pageId;
        return this;
    }

    public CallbackParamsBuilder params(Map<CallbackParams.Params, Object> params) {
        this.params = params;
        return this;
    }

    public CallbackParams build() {
        return CallbackParams.builder()
            .type(type)
            .request(request)
            .params(params)
            .version(version)
            .pageId(pageId)
            .caseData(caseData)
            .build();
    }
}
