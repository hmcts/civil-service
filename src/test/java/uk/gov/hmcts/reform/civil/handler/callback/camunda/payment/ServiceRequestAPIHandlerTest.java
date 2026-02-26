package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_API;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_API_HMC;

@ExtendWith(MockitoExtension.class)
public class ServiceRequestAPIHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private PaymentsService paymentsService;

    @Mock
    private PaymentServiceResponse paymentServiceResponse;

    @Mock
    private HearingFeesService hearingFeesService;

    @Mock
    private Time time;

    @Mock
    private HearingNoticeCamundaService camundaService;

    private ServiceRequestAPIHandler handler;
    private ObjectMapper objectMapper;
    private CaseData caseData;
    private CallbackParams params;

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new ServiceRequestAPIHandler(paymentsService, objectMapper, hearingFeesService, camundaService);
        caseData = CaseDataBuilder.builder().buildMakePaymentsCaseData();
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
                .thenReturn(PaymentServiceResponse.builder()
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
                .thenReturn(PaymentServiceResponse.builder()
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
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT)
                .copy();
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
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDateWithHearingFeePBADetails();
            caseData.setClaimIssuedPBADetails(new SRPbaDetails().setServiceReqReference("123456"));
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
        void shouldMakeHearingPaymentServiceRequest_whenInvoked() {
            //Given
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            caseData.setHearingDueDate(LocalDate.now());
            caseData.setHearingFee(new Fee().setCalculatedAmountInPence(BigDecimal.ONE));
            caseData.setClaimValue(new ClaimValue().setStatementOfValueInPennies(BigDecimal.TEN));
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
            CallbackParams localParams = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
            //THEN
            assertThat(handler.camundaActivityId(localParams)).isEqualTo("ServiceRequestAPI");
        }

        @Test
        void shouldHandleException_whenServiceRequestFails() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithHearingDateWithoutClaimIssuedPbaDetails();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
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
            caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataWithoutServiceRequestReference();
            caseData.setHearingDueDate(LocalDate.now().plusWeeks(1));
            caseData.setHearingFeePBADetails(new SRPbaDetails().setServiceReqReference("123"));
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API, ABOUT_TO_SUBMIT);
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

        @Test
        void shouldCalculateFee_whenPaymentStatusIsNull_allocatedTrackIsDefined() {
            when(hearingFeesService.getFeeForHearingSmallClaims(any())).thenReturn(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(10800)));

            when(camundaService.getProcessVariables(any()))
                .thenReturn(new HearingNoticeVariables()
                                .setHearingType("AAA7-TRI"));

            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsNoPaymentStatus();
            caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId(""));
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            SRPbaDetails actual = responseCaseData.getHearingFeePBADetails();
            SRPbaDetails expected = new SRPbaDetails()
                .setFee(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(10800)))
                .setServiceReqReference(SUCCESSFUL_PAYMENT_REFERENCE)
                ;

            assertThat(actual).isEqualTo(expected);
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
        }

        @Test
        void shouldCalculateFee_whenPaymentStatusIsNull_responseTrackIsDefined() {
            when(hearingFeesService.getFeeForHearingSmallClaims(any())).thenReturn(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(10800)));

            when(camundaService.getProcessVariables(any()))
                .thenReturn(new HearingNoticeVariables()
                                .setHearingType("AAA7-TRI"));

            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsNoPaymentStatus();
            caseData.setAllocatedTrack(null);
            caseData.setResponseClaimTrack(AllocatedTrack.SMALL_CLAIM.name());
            caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId(""));
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            SRPbaDetails actual = responseCaseData.getHearingFeePBADetails();
            SRPbaDetails expected = new SRPbaDetails()
                .setFee(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(10800)))
                .setServiceReqReference(SUCCESSFUL_PAYMENT_REFERENCE)
                ;

            assertThat(actual).isEqualTo(expected);
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
        }

        @ParameterizedTest
        @CsvSource({
            "AAA7-DIS",
            "AAA7-DRH"
        })
        void shouldNotCalculateFee_whenHearingTypeIs(String hearingType) {
            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsPaymentSuccess();
            caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId(""));

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);

            verifyNoInteractions(hearingFeesService);
            verifyNoInteractions(paymentsService);
        }

        @Test
        void shouldNotCalculateFee_whenPaymentStatusIsSuccess() {
            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsPaymentSuccess();
            caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId(""));

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);

            verifyNoInteractions(hearingFeesService);
            verifyNoInteractions(paymentsService);
        }

        @Test
        void shouldNotCalculateFee_whenPaymentStatusIsFailed() {
            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsPaymentFailed();
            caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId(""));

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);

            verifyNoInteractions(hearingFeesService);
            verifyNoInteractions(paymentsService);
        }

        @Test
        void shouldHandleException_whenPaymentRequestFails() {
            when(camundaService.getProcessVariables(any()))
                .thenReturn(new HearingNoticeVariables()
                                .setHearingType("AAA7-TRI"));

            caseData = CaseDataBuilder.builder().withHearingFeePBADetailsNoPaymentStatus();
            caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId(""));

            when(paymentsService.createServiceRequest(any(), any()))
                .thenThrow(FeignException.class);

            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_API_HMC, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
        }
    }
}
