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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_API;

@SpringBootTest(classes = {
    ServiceRequestAPIHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class ServiceRequestAPIHandlerTest extends BaseCallbackHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";

    @MockBean
    private FeesService feesService;

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

        private final Fee feeData = Fee.builder()
            .code("CODE")
            .calculatedAmountInPence(BigDecimal.valueOf(100))
            .build();

        @BeforeEach
        void setup() {
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            given(feesService.getFeeDataByClaimValue(any())).willReturn(feeData);
        }

        @Test
        void shouldMakePaymentServiceRequest_whenInvoked() throws Exception {
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                            .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            caseData = caseData.toBuilder()
                .totalClaimAmount(new BigDecimal(1000)).build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
            assertThat(extractPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldMakeHearingPaymentServiceRequest_whenInvoked() throws Exception {
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            caseData = caseData.toBuilder()
                .hearingDate(LocalDate.now().plusDays(20))
                .hearingFee(
                    Fee.builder()
                        .code("code")
                        .version("version")
                        .calculatedAmountInPence(new BigDecimal(100))
                        .build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(new BigDecimal(1000)).build())
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
            assertThat(extractHearingPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(CREATE_SERVICE_REQUEST_API);
        }
    }

    private SRPbaDetails
        extractPaymentDetailsFromResponse(AboutToStartOrSubmitCallbackResponse response) {
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        return responseCaseData.getClaimIssuedPBADetails();
    }

    private SRPbaDetails
    extractHearingPaymentDetailsFromResponse(AboutToStartOrSubmitCallbackResponse response) {
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        return responseCaseData.getHearingFeePBADetails();
    }
}
