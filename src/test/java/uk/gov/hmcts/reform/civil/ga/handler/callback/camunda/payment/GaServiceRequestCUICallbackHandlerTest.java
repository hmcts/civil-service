package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.payment;

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
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CUI_GENERAL_APP;

@SpringBootTest(classes = {
    GaServiceRequestCUICallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class GaServiceRequestCUICallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";

    @MockBean
    private PaymentsService paymentsService;

    @Autowired
    private GaServiceRequestCUICallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    private GeneralApplicationCaseData caseData;
    private CallbackParams params;

    @BeforeEach
    public void setup() {
        caseData = GeneralApplicationCaseData.builder()
             .ccdCaseReference(1644495739087775L)
                .generalAppPBADetails(GAPbaDetails.builder()
                        .fee(Fee.builder()
                                .calculatedAmountInPence(BigDecimal.valueOf(100))
                                .code("CODE").build()).build())
            .build();
    }

    @Nested
    class MakeServiceRequestPayments {

        @BeforeEach
        void setup() {
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_GENERAL_APP, ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldMakePaymentServiceRequestForClaimFee_whenInvoked() {
            //GIVEN
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_GENERAL_APP, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequestGa(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verify(paymentsService).createServiceRequestGa(caseData, "BEARER_TOKEN");
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            String serviceRequestReference = responseCaseData.getGeneralAppPBADetails().getServiceReqReference();
            assertThat(serviceRequestReference).isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakeAnyServiceRequest_whenServiceRequestHasBeenInvokedPreviously() {
            //GIVEN
            caseData = caseData.toBuilder()
                    .generalAppPBADetails(GAPbaDetails.builder()
                            .serviceReqReference(GeneralApplicationCaseDataBuilder.CUSTOMER_REFERENCE).build())
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_GENERAL_APP, ABOUT_TO_SUBMIT);
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verifyNoInteractions(paymentsService);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            String serviceRequestReference = responseCaseData.getGeneralAppPBADetails().getServiceReqReference();
            assertThat(serviceRequestReference).isEqualTo(GeneralApplicationCaseDataBuilder.CUSTOMER_REFERENCE);
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            //THEN
            assertThat(handler.handledEvents()).contains(CREATE_SERVICE_REQUEST_CUI_GENERAL_APP);
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            //GIVEN
            CallbackParams params =  params = callbackParamsOf(caseData,
                                                               CREATE_SERVICE_REQUEST_CUI_GENERAL_APP, ABOUT_TO_SUBMIT);
            //THEN
            assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo("CreateServiceRequestCUI");
        }

        @Test
        void shouldHandleException_whenServiceRequestFails() {
            //GIVEN
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_GENERAL_APP, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequestGa(any(), any()))
                .thenThrow(FeignException.class);
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            assertThat(response.getErrors()).isNotEmpty();
        }
    }
}
