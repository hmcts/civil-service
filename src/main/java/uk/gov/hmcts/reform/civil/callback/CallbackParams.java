package uk.gov.hmcts.reform.civil.callback;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BaseCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

@Builder(toBuilder = true)
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
