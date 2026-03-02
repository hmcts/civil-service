package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class CitizenClaimIssueFeePaymentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CitizenClaimIssueFeePaymentCallbackHandler handler;

    @Mock
    private ObjectMapper mapper;

    @Test
    void citizenClaimIssuePayment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData.setClaimIssuedPaymentDetails(buildPaymentDetails());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CITIZEN_CLAIM_ISSUE_PAYMENT);
    }

    private PaymentDetails buildPaymentDetails() {
        return new PaymentDetails()
            .setStatus(PaymentStatus.SUCCESS)
            .setReference("R1234-1234-1234-1234")
            ;

    }
}
