package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_API;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_API_HMC;

@SpringBootTest(classes = {
    ServiceRequestAPIHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class ServiceRequestAPIHandlerTest extends BaseCallbackHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";

    @MockBean
    private PaymentsService paymentsService;

    @MockBean
    private PaymentServiceResponse paymentServiceResponse;

    @MockBean
    private HearingFeesService hearingFeesService;

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

        @BeforeEach
        void setup() {
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldMakePaymentServiceRequestForClaimFee_whenInvoked() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithoutServiceRequestReference();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getClaimIssuedPBADetails().getServiceReqReference();
            assertThat(serviceRequestReference).isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldMakePaymentServiceRequestForClaimFee_whenInvokedWithoutClaimIssuedPbaDetails() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithoutClaimIssuedPbaDetails();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(paymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getClaimIssuedPBADetails().getServiceReqReference();
            assertThat(serviceRequestReference).isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakePaymentServiceRequestForClaimFee_whenInvokedWithClaimIssuedPbaDetails() {
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verifyNoInteractions(paymentsService);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getClaimIssuedPBADetails().getServiceReqReference();
            assertThat(serviceRequestReference).isEqualTo(CaseDataBuilder.CUSTOMER_REFERENCE);
        }

        @Test
        void shouldMakePaymentServiceRequestForHearingFee_whenInvoked() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDate();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(paymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getHearingFeePBADetails().getServiceReqReference();
            assertThat(serviceRequestReference).isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakePaymentServiceRequestForHearingFee_whenServiceRequestWasAlreadyIssued() {
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDueDateWithHearingFeePBADetails();
            params =  callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verifyNoInteractions(paymentsService);
        }

        @Test
        void shouldMakePaymentServiceRequestForHearingFee_whenInvokedWithoutClaimIssuedPbaDetails() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDateWithoutClaimIssuedPbaDetails();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(paymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getHearingFeePBADetails().getServiceReqReference();
            assertThat(serviceRequestReference).isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakePaymentServiceRequestForHearingFee_whenInvokedWithClaimIssuedPbaDetails() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDateWithHearingFeePBADetails();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT).toBuilder().build();
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verifyNoInteractions(paymentsService);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getHearingFeePBADetails().getServiceReqReference();
            assertThat(serviceRequestReference).isEqualTo(CaseDataBuilder.CUSTOMER_REFERENCE);
        }

        @Test
        void shouldNotMakeAnyServiceRequest_whenServiceRequestHasBeenInvokedPreviously() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDateWithHearingFeePBADetails()
                .toBuilder()
                .claimIssuedPBADetails(SRPbaDetails.builder().serviceReqReference("123456").build())
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verifyNoInteractions(paymentsService);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getHearingFeePBADetails().getServiceReqReference();
            assertThat(serviceRequestReference).isEqualTo(CaseDataBuilder.CUSTOMER_REFERENCE);
        }

        @Test
        void shouldMakeHearingPaymentServiceRequest_whenInvoked() throws Exception {
            //Given
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            caseData = caseData.toBuilder()
                .hearingDueDate(LocalDate.now())
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.ONE).build())
                .claimValue(ClaimValue.builder().statementOfValueInPennies(BigDecimal.TEN).build()).build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
            assertThat(extractHearingPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            //THEN
            assertThat(handler.handledEvents()).contains(CREATE_SERVICE_REQUEST_API);
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            //GIVEN
            CallbackParams params =  params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            //THEN
            assertThat(handler.camundaActivityId(params)).isEqualTo("ServiceRequestAPI");
        }

        @Test
        void shouldHandleException_whenServiceRequestFails() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDateWithoutClaimIssuedPbaDetails();
            params =  params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequest(any(), any()))
                .thenThrow(FeignException.class);
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldNotMakePaymentServiceRequestForClaimFee_whenInvoked() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithoutServiceRequestReference()
                .toBuilder().hearingDueDate(LocalDate.now().plusWeeks(1))
                .hearingFeePBADetails(SRPbaDetails.builder().serviceReqReference("123").build()).build();
            params =  params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verifyNoInteractions(paymentsService);
            assertThat(response.getErrors()).isEmpty();
        }

        private SRPbaDetails extractHearingPaymentDetailsFromResponse(AboutToStartOrSubmitCallbackResponse response) {
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            return responseCaseData.getHearingFeePBADetails();
        }
    }

    @Nested
    class MakeServiceRequestPaymentsHMC {

        @BeforeEach
        void setup() {
            when(hearingFeesService.getFeeForHearingSmallClaims(any())).thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10800)).build());
        }

        @Test
        void shouldCalculateFee_whenPaymentStatusIsNull() {
            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsNoPaymentStatus();
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            SRPbaDetails actual = responseCaseData.getHearingFeePBADetails();
            SRPbaDetails expected = SRPbaDetails.builder()
                .fee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10800)).build())
                .serviceReqReference(SUCCESSFUL_PAYMENT_REFERENCE)
                .build();

            assertThat(actual).isEqualTo(expected);
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
        }

        @Test
        void shouldNotCalculateFee_whenPaymentStatusIsSuccess() {
            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsPaymentSuccess();

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            verifyNoInteractions(hearingFeesService);
            verifyNoInteractions(paymentsService);
        }

        @Test
        void shouldNotCalculateFee_whenPaymentStatusIsFailed() {
            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsPaymentFailed();

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            verifyNoInteractions(hearingFeesService);
            verifyNoInteractions(paymentsService);
        }

        @Test
        void shouldHandleException_whenPaymentRequestFails() {
            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsNoPaymentStatus();

            when(paymentsService.createServiceRequest(any(), any()))
                .thenThrow(FeignException.class);

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
        }
    }
}
