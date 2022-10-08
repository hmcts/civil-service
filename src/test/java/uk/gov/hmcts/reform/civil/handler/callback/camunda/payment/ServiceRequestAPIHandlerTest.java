package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearing.HearingFeeServiceRequestDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_API;

@SpringBootTest(classes = {
    ServiceRequestAPIHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class ServiceRequestAPIHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";

    @MockBean
    private PaymentsService paymentsService;

    @MockBean
    private PaymentServiceResponse paymentServiceResponse;

    @MockBean
    private Time time;

    @Autowired
    private ServiceRequestAPIHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    private CaseData caseData;
    private CallbackParams params;

    @BeforeEach
    public void setup() {
        caseData = CaseDataBuilder.builder().buildMakePaymentsCaseData();

        when(time.now()).thenReturn(LocalDateTime.of(2020, 1, 1, 12, 0, 0));
    }

    @Nested
    class MakeServiceRequestPayments {

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(CREATE_SERVICE_REQUEST_API);
        }
    }

    private HearingFeeServiceRequestDetails
        extractPaymentDetailsFromResponse(AboutToStartOrSubmitCallbackResponse response) {
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        return responseCaseData.getHearingFeeServiceRequestDetails();
    }
}
