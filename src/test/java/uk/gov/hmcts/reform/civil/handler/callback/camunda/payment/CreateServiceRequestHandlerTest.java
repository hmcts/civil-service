package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class CreateServiceRequestHandlerTest extends BaseCallbackHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";
    private static final String FREE_PAYMENT_REFERENCE = "FREE";

    @MockBean
    private PaymentsService paymentsService;

    @MockBean
    private PaymentServiceResponse paymentServiceResponse;

    @MockBean
    private Time time;
    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private GaForLipService gaForLipService;

    @Autowired
    private PaymentServiceRequestHandler handler;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private GeneralAppFeesService generalAppFeesService;
    private CaseData caseData;
    private CallbackParams params;

    @BeforeEach
    public void setup() {
        caseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataGA();
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(false);
        when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(false);

        when(time.now()).thenReturn(LocalDateTime.of(2020, 1, 1, 12, 0, 0));
    }

    private GAPbaDetails extractPaymentDetailsFromResponse(AboutToStartOrSubmitCallbackResponse response) {
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        return responseCaseData.getGeneralAppPBADetails();
    }

    @Nested
    class MakeServiceRequestPayments {

        @BeforeEach
        void setup() {
            refreshParams();
        }

        @Test
        void shouldMakePaymentServiceRequest_whenInvoked() throws Exception {
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            when(generalAppFeesService.isFreeApplication(any(GeneralApplicationCaseData.class))).thenReturn(false);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createServiceRequest(any(CaseData.class), eq("BEARER_TOKEN"));
            assertThat(extractPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakePaymentServiceRequest_shouldAddFreePaymentDetails_whenInvoked() {
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(FREE_PAYMENT_REFERENCE).build());
            when(generalAppFeesService.isFreeApplication(any(GeneralApplicationCaseData.class))).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService, never()).createServiceRequest(any(CaseData.class), eq("BEARER_TOKEN"));
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
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(FREE_PAYMENT_REFERENCE).build());
            when(generalAppFeesService.isFreeApplication(any(GeneralApplicationCaseData.class))).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService, never()).createServiceRequest(any(CaseData.class), eq("BEARER_TOKEN"));
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
            when(paymentsService.createServiceRequest(any(), any()))
                .thenThrow(ex);
            when(generalAppFeesService.isFreeApplication(any(GeneralApplicationCaseData.class))).thenReturn(false);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().size())
                .isEqualTo(1);
        }

        @Test
        void shouldNotMakePaymentServiceRequest_ifHelpWithFees_whenInvoked() throws Exception {
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(paymentServiceResponse.builder()
                                .serviceRequestReference(FREE_PAYMENT_REFERENCE).build());
            when(generalAppFeesService.isFreeApplication(any(GeneralApplicationCaseData.class))).thenReturn(false);
            caseData = caseData.toBuilder().generalAppHelpWithFees(HelpWithFees.builder()
                                                                       .helpWithFee(YesOrNo.YES).build()).build();
            refreshParams();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService, never()).createServiceRequest(any(CaseData.class), eq("BEARER_TOKEN"));
            assertThat(extractPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(FREE_PAYMENT_REFERENCE);
            PaymentDetails paymentDetails = extractPaymentDetailsFromResponse(response).getPaymentDetails();
            assertThat(paymentDetails).isNull();
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo("GeneralApplicationPaymentServiceReq");
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(MAKE_PAYMENT_SERVICE_REQ_GASPEC);
        }

        @Test
        void shouldReturnHwf_True() {
            caseData = caseData.toBuilder().generalAppHelpWithFees(HelpWithFees.builder()
                                                                       .helpWithFee(YesOrNo.YES).build()).build();
            assertThat(handler.isHelpWithFees(toGaCaseData(caseData), caseData)).isTrue();
        }

        @Test
        void shouldReturnHwf_False() {
            caseData = caseData.toBuilder().generalAppHelpWithFees(HelpWithFees.builder()
                                                                       .helpWithFee(YesOrNo.NO).build()).build();
            assertThat(handler.isHelpWithFees(toGaCaseData(caseData), caseData)).isFalse();
        }

        @Test
        void shouldReturnFreeLipGa_True() {
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            caseData = caseData.toBuilder().generalAppPBADetails(GAPbaDetails.builder()
                                                                     .fee(Fee.builder().code("FREE").build()).build()).build();
            assertThat(handler.isFreeGaLip(toGaCaseData(caseData), caseData)).isTrue();
        }

        @Test
        void shouldReturnFreeLipGa_whenPbaDetailsAreNull_false() {
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            caseData = caseData.toBuilder().build();
            assertThat(handler.isFreeGaLip(toGaCaseData(caseData), caseData)).isFalse();
        }

        @Test
        void shouldReturnFreeLipGa_whenFeeDetailsAreNull_false() {
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            caseData = caseData.toBuilder().generalAppPBADetails(GAPbaDetails.builder()
                                                                  .build()).build();
            assertThat(handler.isFreeGaLip(toGaCaseData(caseData), caseData)).isFalse();
        }

        @Test
        void shouldReturnFreeLipGa_whenFeeCodeIsNotFree_false() {
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            caseData = caseData.toBuilder().generalAppPBADetails(GAPbaDetails.builder()
                                                                     .fee(Fee.builder().code("1").build()).build())
                                                                     .build();
            assertThat(handler.isFreeGaLip(toGaCaseData(caseData), caseData)).isFalse();
        }

        @Test
        void shouldMakePaymentServiceRequest_whenOnlyGaCaseDataProvided() throws Exception {
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                    .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            when(generalAppFeesService.isFreeApplication(any(GeneralApplicationCaseData.class))).thenReturn(false);

            CallbackParams gaOnlyParams = gaCallbackParamsOf(toGaCaseData(caseData), ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(gaOnlyParams);

            verify(paymentsService).createServiceRequest(any(CaseData.class), eq("BEARER_TOKEN"));
            assertThat(extractPaymentDetailsFromResponse(response).getServiceReqReference())
                .isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }
    }

    private void refreshParams() {
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
    }
}
