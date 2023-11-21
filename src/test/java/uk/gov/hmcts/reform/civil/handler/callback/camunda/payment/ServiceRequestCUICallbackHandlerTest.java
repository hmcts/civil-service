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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CUI;

@SpringBootTest(classes = {
    ServiceRequestCUICallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class ServiceRequestCUICallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";

    @MockBean
    private PaymentsService paymentsService;

    @Autowired
    private ServiceRequestCUICallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    private CaseData caseData;
    private CallbackParams params;

    @BeforeEach
    public void setup() {
        caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee();
    }

    @Nested
    class MakeServiceRequestPayments {

        @BeforeEach
        void setup() {
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI, ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldMakePaymentServiceRequestForClaimFee_whenInvoked() {
            //GIVEN
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequest(any(), any()))
                .thenReturn(PaymentServiceResponse.builder()
                                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE).build());
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verify(paymentsService).createServiceRequest(caseData, "BEARER_TOKEN");
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getServiceRequestReference();
            assertThat(serviceRequestReference).isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakeAnyServiceRequest_whenServiceRequestHasBeenInvokedPreviously() {
            //GIVEN
            caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee().toBuilder()
                .serviceRequestReference(CaseDataBuilder.CUSTOMER_REFERENCE)
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI, ABOUT_TO_SUBMIT);
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            verifyNoInteractions(paymentsService);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getServiceRequestReference();
            assertThat(serviceRequestReference).isEqualTo(CaseDataBuilder.CUSTOMER_REFERENCE);
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            //THEN
            assertThat(handler.handledEvents()).contains(CREATE_SERVICE_REQUEST_CUI);
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            //GIVEN
            CallbackParams params =  params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI, ABOUT_TO_SUBMIT);
            //THEN
            assertThat(handler.camundaActivityId(params)).isEqualTo("CreateServiceRequestCUI");
        }

        @Test
        void shouldHandleException_whenServiceRequestFails() {
            //GIVEN
            params =  params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI, ABOUT_TO_SUBMIT);
            when(paymentsService.createServiceRequest(any(), any()))
                .thenThrow(FeignException.class);
            //WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //THEN
            assertThat(response.getErrors()).isNotEmpty();
        }
    }
}
