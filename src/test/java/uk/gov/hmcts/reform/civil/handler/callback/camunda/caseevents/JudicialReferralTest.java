package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

public class JudicialReferralTest extends BaseCallbackHandlerTest {

    private JudicialReferralCallbackHandler callbackHandler;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void prepare() {
        callbackHandler = new JudicialReferralCallbackHandler(objectMapper);
    }

    @Test
    void handleSubmit() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) callbackHandler
            .handle(params);

        Map<String, Object> expectedData = caseData.toMap(objectMapper);
        Assertions.assertEquals(expectedData, response.getData());
    }
}
