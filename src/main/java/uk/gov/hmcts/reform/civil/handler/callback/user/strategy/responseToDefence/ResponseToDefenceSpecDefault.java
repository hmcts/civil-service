package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.responseToDefence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Component
public class ResponseToDefenceSpecDefault extends ResponseToDefenceSpecStrategy{
    @Override
    public CallbackResponse populateCaseData(CallbackParams callbackParams, ObjectMapper objectMapper) {
        CaseData updatedData = updateCaseDataWithRespondent1Copy(callbackParams);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.toMap(objectMapper))
            .build();
    }

}
