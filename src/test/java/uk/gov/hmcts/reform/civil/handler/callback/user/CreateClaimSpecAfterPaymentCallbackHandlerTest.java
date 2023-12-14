package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;

@SpringBootTest(classes = {
    CreateClaimSpecAfterPaymentCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
class CreateClaimSpecAfterPaymentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private CreateClaimSpecAfterPaymentCallbackHandler handler;

    @Test
    void shouldRespondWithStateChanged() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_CLAIM_SPEC_AFTER_PAYMENT);
    }
}
