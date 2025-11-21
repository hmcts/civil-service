package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentCallbackHandlerTest extends BaseCallbackHandlerTest {

    private CreateClaimAfterPaymentCallbackHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
        handler = new CreateClaimAfterPaymentCallbackHandler(objectMapper);
    }

    @Test
    void shouldRespondWithStateChanged() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(response.getErrors()).isNull();
        assertThat(updatedData.getBusinessProcess()).isNotNull();
        assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(CREATE_CLAIM_AFTER_PAYMENT.name());
        assertThat(updatedData.getBusinessProcess().getStatus()).isEqualTo(READY);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_CLAIM_AFTER_PAYMENT);
    }
}
