package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_PARTIES_NOTIFIED_HMC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;

@SpringBootTest(classes = {
    UpdateCaseProgressHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class UpdateCaseProgressHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private UpdateCaseProgressHandler handler;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnCallbackResponseWithHearingReadinessCaseStateOnAboutToSubmit() {
        var caseData = CaseData.builder().build();

        var params = callbackParamsOf(caseData.toMap(objectMapper), UPDATE_PARTIES_NOTIFIED_HMC.name(), CallbackType.ABOUT_TO_SUBMIT);

        var result = handler.handle(params);

        assertEquals(result, AboutToStartOrSubmitCallbackResponse.builder()
            .state(HEARING_READINESS.name())
            .build());
    }

}
