package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    LIPClaimSettledCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
public class LIPClaimSettledCallBackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private LIPClaimSettledCallbackHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCallSubmitClaimSettledCUIUponAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getErrors()).isNull();
        }
    }

}
