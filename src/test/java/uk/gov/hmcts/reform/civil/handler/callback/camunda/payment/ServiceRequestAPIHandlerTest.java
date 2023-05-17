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
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
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
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldMakePaymentServiceRequestForClaimFee_whenInvoked() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithoutServiceRequestReference();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
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
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
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
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
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
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verifyNoInteractions(paymentsService);
        }

        @Test
        void shouldMakePaymentServiceRequestForHearingFee_whenInvokedWithoutClaimIssuedPbaDetails() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDateWithoutClaimIssuedPbaDetails();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
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
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
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
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
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
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
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
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //THEN
            assertThat(handler.camundaActivityId(params)).isEqualTo("ServiceRequestAPI");
        }

        @Test
        void shouldHandleException_whenServiceRequestFails() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDateWithoutClaimIssuedPbaDetails();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
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
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
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

        @Test
        void shouldMakeHearingPaymentServiceRequest_whenInvokedAndIsNoticeOfChange() {
            //Given
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            caseData = caseData.toBuilder()
                .hearingDueDate(LocalDate.now())
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.ONE).build())
                .claimValue(ClaimValue.builder().statementOfValueInPennies(BigDecimal.TEN).build())
                .hearingUnpaidAfterNocFlag("NEW_REP_ALLOW_SERVICE_REQUEST")
                .changeOfRepresentation(ChangeOfRepresentation.builder().caseRole("[APPLICANTSOLICITORONE]").build())
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
            assertThat(extractHearingPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
            assertThat(response.getData().get("hearingUnpaidAfterNocFlag")).isEqualTo("CURRENT_REP_HAS_SERVICE_REQUEST");
        }

        @Test
        void shouldNotMakeHearingPaymentServiceRequest_whenInvokedAndIsSameNoticeOfChange() {
            // When a NOC is applied to a case to change claimant rep, we generate a service request, but we then want
            // to block additional service requests for that claimant rep, e.g. if hearing notice event is retriggered
            // after a NOC has been applied
            //Given
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            caseData = caseData.toBuilder()
                .hearingDueDate(LocalDate.now())
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.ONE).build())
                .claimValue(ClaimValue.builder().statementOfValueInPennies(BigDecimal.TEN).build())
                .hearingUnpaidAfterNocFlag("CURRENT_REP_HAS_SERVICE_REQUEST")
                .changeOfRepresentation(ChangeOfRepresentation.builder().caseRole("[APPLICANTSOLICITORONE]").build())
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //Then
            verifyNoInteractions(paymentsService);
        }

        @Test
        void shouldNotMakeHearingPaymentServiceRequest_whenInvokedNoticeOfChangeRespondent() {
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            caseData = caseData.toBuilder()
                .hearingDueDate(LocalDate.now())
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.ONE).build())
                .claimValue(ClaimValue.builder().statementOfValueInPennies(BigDecimal.TEN).build())
                .hearingUnpaidAfterNocFlag("CURRENT_REP_HAS_SERVICE_REQUEST")
                .changeOfRepresentation(ChangeOfRepresentation.builder().caseRole("[RESPONDENTSOLICITORONE]").build())
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //Then
            verifyNoInteractions(paymentsService);
        }
    }
}
