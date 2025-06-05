package uk.gov.hmcts.reform.civil.callback;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.wa.WaMapper;

import java.util.Map;

@Builder(toBuilder = true)
@Getter
public class CallbackParams {

    private CallbackType type;
    private CallbackRequest request;
    private Map<Params, Object> params;
    private CallbackVersion version;
    private String pageId;
    private CaseData caseData;
    private CaseData caseDataBefore;
    private WaMapper waMapper;

    public enum Params {
        BEARER_TOKEN
    }
}
