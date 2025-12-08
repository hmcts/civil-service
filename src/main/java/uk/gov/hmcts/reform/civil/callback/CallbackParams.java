package uk.gov.hmcts.reform.civil.callback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallbackParams {

    private CallbackType type;
    private CallbackRequest request;
    private Map<Params, Object> params;
    private CallbackVersion version;
    private String pageId;
    private CaseData caseData;
    private CaseData caseDataBefore;

    public enum Params {
        BEARER_TOKEN
    }
}
