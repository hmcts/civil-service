package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackParams.Params;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CUI_GENERAL_APP;

@ExtendWith(MockitoExtension.class)
class ServiceRequestCUICallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private PaymentsService paymentsService;

    private ServiceRequestCUICallbackHandler handler;
    private ObjectMapper objectMapper;
    private CaseData caseData;
    private CallbackParams params;

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module());
        handler = new ServiceRequestCUICallbackHandler(paymentsService, objectMapper);
        caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee();
    }

    @Nested
    class MakeServiceRequestPayments {

        @BeforeEach
        void setup() {
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldMakePaymentServiceRequestForClaimFee_whenInvoked() {
            caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee().toBuilder()
                .serviceRequestReference(null)
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                    .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(paymentsService).createServiceRequest(any(CaseData.class), eq("BEARER_TOKEN"));
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseCaseData.getServiceRequestReference()).isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakeAnyServiceRequest_whenServiceRequestHasBeenInvokedPreviously() {
            caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee().toBuilder()
                .serviceRequestReference(CaseDataBuilder.CUSTOMER_REFERENCE)
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(paymentsService);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseCaseData.getServiceRequestReference()).isEqualTo(CaseDataBuilder.CUSTOMER_REFERENCE);
        }

        @Test
        void shouldNotMakeAnyServiceRequest_whenClaimantHasRequestedHelpWithFees() {
            caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                    .helpWithFees(HelpWithFees.builder()
                        .helpWithFee(YesOrNo.YES)
                        .build())
                    .build())
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verifyNoInteractions(paymentsService);
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents())
                .contains(CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, CREATE_SERVICE_REQUEST_CUI_GENERAL_APP);
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            CallbackParams localParams = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);

            assertThat(handler.camundaActivityId(localParams)).isEqualTo("CreateServiceRequestCUI");
        }
    }

    @Nested
    class MakeServiceRequestPaymentsForGa {

        private CaseData gaCaseData;
        private GeneralApplicationCaseData gaDto;

        @BeforeEach
        void setupGa() {
            gaCaseData = CaseDataBuilder.builder().buildMakePaymentsCaseDataGA()
                .toBuilder()
                .generalAppPBADetails(GAPbaDetails.builder()
                    .fee(gaFee())
                    .build())
                .build();
            gaDto = GeneralApplicationCaseDataBuilder.builder()
                .withCcdCaseReference(gaCaseData.getCcdCaseReference())
                .withGeneralAppPBADetails(gaCaseData.getGeneralAppPBADetails())
                .withGeneralAppHelpWithFees(null)
                .build();
        }

        @Test
        void shouldMakePaymentServiceRequestForGeneralApp_whenInvoked() {
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                    .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(buildGaParams());

            verify(paymentsService).createServiceRequest(any(CaseData.class), eq("BEARER_TOKEN"));
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseCaseData.getGeneralAppPBADetails().getServiceReqReference())
                .isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakePaymentServiceRequestForGeneralApp_whenHwf() {
            gaDto = gaDto.toBuilder()
                .generalAppHelpWithFees(HelpWithFees.builder()
                    .helpWithFee(YesOrNo.YES)
                    .build())
                .build();

            handler.handle(buildGaParams());

            verifyNoInteractions(paymentsService);
        }

        @Test
        void shouldNotMakePaymentServiceRequestForGeneralApp_whenServiceRequestExists() {
            gaCaseData = gaCaseData.toBuilder()
                .generalAppPBADetails(gaCaseData.getGeneralAppPBADetails().toBuilder()
                    .serviceReqReference(SUCCESSFUL_PAYMENT_REFERENCE)
                    .build())
                .build();
            gaDto = gaDto.toBuilder()
                .generalAppPBADetails(gaCaseData.getGeneralAppPBADetails())
                .build();

            handler.handle(buildGaParams());

            verifyNoInteractions(paymentsService);
        }

        private CallbackParams buildGaParams() {
            return CallbackParams.builder()
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                    .eventId(CREATE_SERVICE_REQUEST_CUI_GENERAL_APP.name())
                    .caseDetails(CaseDetails.builder()
                        .id(CASE_ID)
                        .data(new HashMap<>())
                        .build())
                    .build())
                .params(Map.of(Params.BEARER_TOKEN, "BEARER_TOKEN"))
                .caseData(gaCaseData)
                .gaCaseData(gaDto)
                .build();
        }

        private Fee gaFee() {
            return Fee.builder()
                .code("FE203")
                .version("1")
                .calculatedAmountInPence(BigDecimal.valueOf(27500))
                .build();
        }
    }

    @Nested
    class ExceptionHandling {

        @Test
        void shouldReturnError_whenFeignExceptionThrown() {
            caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee().toBuilder()
                .serviceRequestReference(null)
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);
            FeignException feignException = Mockito.mock(FeignException.class);

            when(paymentsService.createServiceRequest(any(), any())).thenThrow(feignException);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
        }
    }
}
