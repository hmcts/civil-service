package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.PaymentsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE;

@ExtendWith(MockitoExtension.class)
public class ServiceRequestCUICallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private PaymentsService paymentsService;

    private ServiceRequestCUICallbackHandler handler;
    private ObjectMapper objectMapper;
    private CaseData caseData;
    private CallbackParams params;

    private static final String SUCCESSFUL_PAYMENT_REFERENCE = "2022-1655915218557";

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
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
            // GIVEN
            caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee().toBuilder()
                .serviceRequestReference(SUCCESSFUL_PAYMENT_REFERENCE)
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);

            // WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // THEN
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getServiceRequestReference();
            assertThat(serviceRequestReference).isEqualTo(SUCCESSFUL_PAYMENT_REFERENCE);
        }

        @Test
        void shouldNotMakeAnyServiceRequest_whenServiceRequestHasBeenInvokedPreviously() {
            // GIVEN
            caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee().toBuilder()
                .serviceRequestReference(CaseDataBuilder.CUSTOMER_REFERENCE)
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);

            // WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // THEN
            verifyNoInteractions(paymentsService);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String serviceRequestReference = responseCaseData.getServiceRequestReference();
            assertThat(serviceRequestReference).isEqualTo(CaseDataBuilder.CUSTOMER_REFERENCE);
        }

        @Test
        void shouldNotMakeAnyServiceRequest_whenClaimantHasRequestedHelpWithFees() {
            // GIVEN
            caseData = CaseDataBuilder.builder().buildCuiCaseDataWithFee().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                                 .helpWithFees(
                                     HelpWithFees.builder()
                                         .helpWithFee(YesOrNo.YES)
                                         .build()
                                 ).build())
                .build();
            params = callbackParamsOf(caseData, CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);

            // THEN
            verifyNoInteractions(paymentsService);
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            // THEN
            assertThat(handler.handledEvents()).contains(CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE);
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            // GIVEN
            CallbackParams localParams = callbackParamsOf(caseData,
                                                          CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE, ABOUT_TO_SUBMIT);

            // THEN
            assertThat(handler.camundaActivityId(localParams)).isEqualTo("CreateServiceRequestCUI");
        }

        @Test
        void shouldHandleException_whenServiceRequestFails() {
            // GIVEN
            when(paymentsService.createServiceRequest(any(), any()))
                .thenThrow(FeignException.class);

            // WHEN
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // THEN
            assertThat(response.getErrors()).isNotEmpty();
        }
    }
}
