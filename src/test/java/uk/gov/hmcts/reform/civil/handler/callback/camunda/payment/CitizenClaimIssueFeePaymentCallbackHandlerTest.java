package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;

@SpringBootTest(classes = {
    CitizenClaimIssueFeePaymentCallbackHandler.class,
    JacksonAutoConfiguration.class,
})

class CitizenClaimIssueFeePaymentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private CitizenClaimIssueFeePaymentCallbackHandler handler;

    @Test
    void citizenClaimIssuePayment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData = caseData.toBuilder().claimIssuedPaymentDetails(buildPaymentDetails()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
    }

    private PaymentDetails buildPaymentDetails() {
        return PaymentDetails.builder()
            .status(PaymentStatus.SUCCESS)
            .reference("R1234-1234-1234-1234")
            .build();

    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CITIZEN_CLAIM_ISSUE_PAYMENT);
    }
}
