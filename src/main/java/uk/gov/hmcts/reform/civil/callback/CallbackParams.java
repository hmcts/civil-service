package uk.gov.hmcts.reform.civil.callback;

import lombok.Getter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BaseCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

@Accessors(chain = true)
@Getter
public class CallbackParams {

    private CallbackType type;
    private CallbackRequest request;
    private Map<Params, Object> params;
    private CallbackVersion version;
    private String pageId;
    private BaseCaseData caseData;
    private BaseCaseData caseDataBefore;
    private boolean isGeneralApplicationCaseType;
    private boolean isCivilCaseType;

    public CallbackParams type(CallbackType type) {
        this.type = type;
        return this;
    }

    public CallbackParams request(CallbackRequest request) {
        this.request = request;
        return this;
    }

    public CallbackParams request(CallbackRequest.CallbackRequestBuilder requestBuilder) {
        return request(requestBuilder.build());
    }

    public CallbackParams params(Map<Params, Object> params) {
        this.params = params;
        return this;
    }

    public CallbackParams version(CallbackVersion version) {
        this.version = version;
        return this;
    }

    public CallbackParams pageId(String pageId) {
        this.pageId = pageId;
        return this;
    }

    public CallbackParams caseData(BaseCaseData caseData) {
        this.caseData = caseData;
        return this;
    }

    public CallbackParams caseDataBefore(BaseCaseData caseDataBefore) {
        this.caseDataBefore = caseDataBefore;
        return this;
    }

    public CallbackParams isGeneralApplicationCaseType(boolean isGeneralApplicationCaseType) {
        this.isGeneralApplicationCaseType = isGeneralApplicationCaseType;
        return this;
    }

    public CallbackParams isCivilCaseType(boolean isCivilCaseType) {
        this.isCivilCaseType = isCivilCaseType;
        return this;
    }

    public CallbackParams copy() {
        return new CallbackParams()
            .type(this.type)
            .request(this.request)
            .params(this.params)
            .version(this.version)
            .pageId(this.pageId)
            .caseData(this.caseData)
            .caseDataBefore(this.caseDataBefore)
            .isGeneralApplicationCaseType(this.isGeneralApplicationCaseType)
            .isCivilCaseType(this.isCivilCaseType);
    }

    public CaseData getCaseData() {
        if (caseData instanceof CaseData civilCaseData) {
            return civilCaseData;
        }
        throw new IllegalStateException("CallbackParams does not contain CaseData");
    }

    public CaseData getCaseDataBefore() {
        if (null == caseDataBefore) {
            return null;
        } else if (caseDataBefore instanceof CaseData civilCaseData) {
            return civilCaseData;
        }
        throw new IllegalStateException("CallbackParams does not contain CaseDataBefore");
    }

    public GeneralApplicationCaseData getGeneralApplicationCaseData() {
        if (caseData instanceof GeneralApplicationCaseData gaCaseData) {
            return gaCaseData;
        }
        throw new IllegalStateException("CallbackParams does not contain GeneralApplicationCaseData");
    }

    public GeneralApplicationCaseData getGeneralApplicationCaseDataBefore() {
        if (null == caseDataBefore) {
            return null;
        } else if (caseDataBefore instanceof GeneralApplicationCaseData gaCaseData) {
            return gaCaseData;
        }
        throw new IllegalStateException("CallbackParams does not contain GeneralApplicationCaseDataBefore");
    }

    public enum Params {
        BEARER_TOKEN
    }

}
