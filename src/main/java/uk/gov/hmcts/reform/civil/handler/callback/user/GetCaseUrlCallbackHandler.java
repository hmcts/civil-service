package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.GetCaseCallbackResponse;
import uk.gov.hmcts.reform.civil.CaseDefinitionConstants;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
@RequiredArgsConstructor
public class GetCaseUrlCallbackHandler {

    public GetCaseCallbackResponse setWAToggleVal(CallbackParams callbackParams) {
        GetCaseCallbackResponse getCaseCallbackResponse
            = new GetCaseCallbackResponse();
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.featureToggleWA(CaseDefinitionConstants.WA_3_5);
        return getCaseCallbackResponse;
    }
}
