package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_PAYMENT_SERVICE_REQ_GASPEC;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@SpringBootTest(classes = {
    PaymentServiceRequestHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class PaymentServiceRequestHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";
    private static final String FREE_PAYMENT_REFERENCE = "FREE";

    @MockBean
    private PaymentsService paymentsService;

    @MockBean
    private PaymentServiceResponse paymentServiceResponse;

    @MockBean
    private Time time;

    @MockBean
    private GaForLipService gaForLipService;

    @Autowired
    private PaymentServiceRequestHandler handler;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private GeneralAppFeesService generalAppFeesService;
    private GeneralApplicationCaseData caseData;
    private CallbackParams params;

    @BeforeEach
    public void setup() {
        caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();
        when(gaForLipService.isGaForLip(any())).thenReturn(false);

        when(time.now()).thenReturn(LocalDateTime.of(2020, 1, 1, 12, 0, 0));
    }

    private GeneralApplicationPbaDetails extractPaymentDetailsFromResponse(AboutToStartOrSubmitCallbackResponse response) {
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        return responseCaseData.getGeneralAppPBADetails();
    }

    @Nested
    class MakeServiceRequestPayments {

        @BeforeEach
        void setup() {
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldMakePaymentServiceRequest_whenInvoked() {
            when(paymentsService.createServiceRequestGa(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            when(generalAppFeesService.isFreeApplication(any())).thenReturn(false);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createServiceRequestGa(caseData, "BEARER_TOKEN");
            assertThat(extractPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakePaymentServiceRequest_shouldAddFreePaymentDetails_whenInvoked() {
            when(paymentsService.createServiceRequestGa(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(FREE_PAYMENT_REFERENCE).build());
            when(generalAppFeesService.isFreeApplication(any())).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService, never()).createServiceRequestGa(caseData, "BEARER_TOKEN");
            assertThat(extractPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(FREE_PAYMENT_REFERENCE);
            PaymentDetails paymentDetails = extractPaymentDetailsFromResponse(response).getPaymentDetails();
            assertThat(paymentDetails).isNotNull();
            assertThat(paymentDetails.getStatus()).isEqualTo(SUCCESS);
            assertThat(paymentDetails.getCustomerReference()).isEqualTo(FREE_PAYMENT_REFERENCE);
            assertThat(paymentDetails.getReference()).isEqualTo(FREE_PAYMENT_REFERENCE);
            assertThat(extractPaymentDetailsFromResponse(response).getPaymentSuccessfulDate())
                .isNotNull();
        }

        @Test
        void shouldNotMakePaymentServiceRequest_shouldAddFreePaymentDetails_for_Lip_whenInvoked() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(paymentsService.createServiceRequestGa(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(FREE_PAYMENT_REFERENCE).build());
            when(generalAppFeesService.isFreeApplication(any())).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService, never()).createServiceRequestGa(caseData, "BEARER_TOKEN");
            assertThat(extractPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(FREE_PAYMENT_REFERENCE);
            PaymentDetails paymentDetails = extractPaymentDetailsFromResponse(response).getPaymentDetails();
            assertThat(paymentDetails).isNotNull();
            assertThat(paymentDetails.getStatus()).isEqualTo(SUCCESS);
            assertThat(paymentDetails.getCustomerReference()).isEqualTo(FREE_PAYMENT_REFERENCE);
            assertThat(paymentDetails.getReference()).isEqualTo(FREE_PAYMENT_REFERENCE);
            assertThat(extractPaymentDetailsFromResponse(response).getPaymentSuccessfulDate())
                .isNotNull();
        }

        @Test
        void shouldThrow_whenPaymentServiceFailed() {
            var ex = Mockito.mock(FeignException.class);
            Mockito.when(ex.status()).thenReturn(404);
            when(paymentsService.createServiceRequestGa(any(), any()))
                .thenThrow(ex);
            when(generalAppFeesService.isFreeApplication(any())).thenReturn(false);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().size())
                .isEqualTo(1);
        }

        @Test
        void shouldNotMakePaymentServiceRequest_ifHelpWithFees_whenInvoked() {
            when(paymentsService.createServiceRequestGa(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(FREE_PAYMENT_REFERENCE).build());
            when(generalAppFeesService.isFreeApplication(any())).thenReturn(false);
            caseData = caseData.toBuilder().generalAppHelpWithFees(new HelpWithFees()
                                                                       .setHelpWithFee(YesOrNo.YES)).build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService, never()).createServiceRequestGa(caseData, "BEARER_TOKEN");
            assertThat(extractPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(FREE_PAYMENT_REFERENCE);
            PaymentDetails paymentDetails = extractPaymentDetailsFromResponse(response).getPaymentDetails();
            assertThat(paymentDetails).isNull();
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo("GeneralApplicationPaymentServiceReq");
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(MAKE_PAYMENT_SERVICE_REQ_GASPEC);
        }

        @Test
        void shouldReturnHwf_True() {
            caseData = caseData.toBuilder().generalAppHelpWithFees(new HelpWithFees()
                                                                       .setHelpWithFee(YesOrNo.YES)).build();
            assertThat(handler.isHelpWithFees(caseData)).isTrue();
        }

        @Test
        void shouldReturnHwf_False() {
            caseData = caseData.toBuilder().generalAppHelpWithFees(new HelpWithFees()
                                                                       .setHelpWithFee(YesOrNo.NO)).build();
            assertThat(handler.isHelpWithFees(caseData)).isFalse();
        }

        @Test
        void shouldReturnFreeLipGa_True() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            caseData = caseData.toBuilder().generalAppPBADetails(GeneralApplicationPbaDetails.builder()
                                                                     .fee(new Fee().setCode("FREE")).build()).build();
            assertThat(handler.isFreeGaLip(caseData)).isTrue();
        }

        @Test
        void shouldReturnFreeLipGa_whenPbaDetailsAreNull_false() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            caseData = caseData.toBuilder().build();
            assertThat(handler.isFreeGaLip(caseData)).isFalse();
        }

        @Test
        void shouldReturnFreeLipGa_whenFeeDetailsAreNull_false() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            caseData = caseData.toBuilder().generalAppPBADetails(GeneralApplicationPbaDetails.builder()
                                                                  .build()).build();
            assertThat(handler.isFreeGaLip(caseData)).isFalse();
        }

        @Test
        void shouldReturnFreeLipGa_whenFeeCodeIsNotFree_false() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            caseData = caseData.toBuilder().generalAppPBADetails(GeneralApplicationPbaDetails.builder()
                                                                     .fee(new Fee().setCode("1")).build())
                                                                     .build();
            assertThat(handler.isFreeGaLip(caseData)).isFalse();
        }
    }

}
