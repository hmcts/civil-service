package uk.gov.hmcts.reform.civil.handler.callback.user;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadHandler.class,
    JacksonAutoConfiguration.class
})
public class EvidenceUploadHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private EvidenceUploadHandler handler;

    @Test
    void givenAboutToStartThenReturnsAboutToStartOrSubmitCallbackResponse() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

        CallbackResponse response = handler.handle(params);

        assertThat(response).isInstanceOf(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Test
    void givenAboutToSubmitThenReturnsAboutToStartOrSubmitCallbackResponse() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        CallbackResponse response = handler.handle(params);

        assertThat(response).isInstanceOf(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Test
    void givenSubmittedThenReturnsSubmittedCallbackResponse() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

        CallbackResponse response = handler.handle(params);

        assertThat(response).isInstanceOf(SubmittedCallbackResponse.class);
    }

    @Test
    void whenRegisterCalledThenReturnEvidenceUploadCaseEvent() {
        Map<String, CallbackHandler> registerTarget = new HashMap<>();
        handler.register(registerTarget);

        assertThat(registerTarget).containsExactly(entry(EVIDENCE_UPLOAD.name(), handler));
    }


}
