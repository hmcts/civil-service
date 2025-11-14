package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.fee;

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
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VALIDATE_FEE_GASPEC;

@SpringBootTest(classes = {
    GaValidateFeeCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class GaValidateFeeCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @MockBean
    private GeneralAppFeesService feesService;
    @MockBean
    private GaForLipService gaForLipService;
    @Autowired
    private GaValidateFeeCallbackHandler handler;
    public static final String VERSION = "1";
    private static final Fee FEE108 = Fee.builder().calculatedAmountInPence(
        BigDecimal.valueOf(10800)).code("FEE0443").version(VERSION).build();
    private static final Fee FEE275 = Fee.builder().calculatedAmountInPence(
        BigDecimal.valueOf(27500)).code("FEE0442").version(VERSION).build();
    private static final Fee FEE14 = Fee.builder().calculatedAmountInPence(
        BigDecimal.valueOf(1400)).code("FEE0458").version("2").build();

    private static final String ERROR_MESSAGE_NO_FEE_IN_CASEDATA = "Application case data does not have fee details";
    private static final String ERROR_MESSAGE_FEE_CHANGED = "Fee has changed since application was submitted. "
        + "It needs to be validated again";
    private static final String TASK_ID = "GeneralApplicationValidateFee";

    private CallbackParams params;

    @Nested
    class MakePBAPayments {

        @BeforeEach
        public void beforeEach() {
            when(gaForLipService.isGaForLip(any())).thenReturn(false);
        }

        //TODO: investigate removal?
        //        @Test
        //        void shouldReturnErrors_whenCaseDataDoesNotHavePBADetails() {
        //            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        //            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //            when(feesService.getFeeForGA(any(GeneralApplicationCaseData.class)))
        //                .thenReturn(Fee.builder().calculatedAmountInPence(
        //                    BigDecimal.valueOf(10800)).code("").version(VERSION).build());
        //
        //            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //
        //            verify(feesService).getFeeForGA(any(GeneralApplicationCaseData.class));
        //            assertThat(response.getErrors()).isNotEmpty();
        //            assertThat(response.getErrors()).contains(ERROR_MESSAGE_NO_FEE_IN_CASEDATA);
        //        }

        //TODO: investigate removal?
        //        @Test
        //        void shouldReturnErrors_whenCaseDataDoesNotHaveFeeDetails() {
        //            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().gaPbaDetails(GAPbaDetails.builder().build()).build();
        //            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //            when(feesService.getFeeForGA(any(GeneralApplicationCaseData.class)))
        //                .thenReturn(Fee.builder().calculatedAmountInPence(
        //                    BigDecimal.valueOf(10800)).code("").version(VERSION).build());
        //
        //            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //
        //            verify(feesService).getFeeForGA(any(GeneralApplicationCaseData.class));
        //            assertThat(response.getErrors()).isNotEmpty();
        //            assertThat(response.getErrors()).contains(ERROR_MESSAGE_NO_FEE_IN_CASEDATA);
        //        }

        //TODO: investigate removal?
        //        @Test
        //        void shouldReturnErrors_whenConsentedApplicationWithDifferentFeesOnCaseDataAndFeeFromFeeService() {
        //
        //            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildFeeValidationCaseData(FEE108, true, false);
        //            when(feesService.getFeeForGA(any(GeneralApplicationCaseData.class)))
        //                .thenReturn(FEE275);
        //            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //
        //            verify(feesService).getFeeForGA(caseData);
        //            assertThat(response.getErrors()).isNotEmpty();
        //            assertThat(response.getErrors()).contains(ERROR_MESSAGE_FEE_CHANGED);
        //        }

        //TODO: investigate removal?
        //        @Test
        //        void shouldReturnNoErrors_whenWithNoticeApplicationSameFeesOnCaseDataAndFeeFromFeeService() {
        //            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildFeeValidationCaseData(FEE275, false, true);
        //            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //            when(feesService.getFeeForGA(any(GeneralApplicationCaseData.class)))
        //                .thenReturn(FEE275);
        //
        //            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //            verify(feesService).getFeeForGA(caseData);
        //            assertThat(response.getErrors()).isEmpty();
        //        }

        //TODO: investigate removal?
        //        @Test
        //        void shouldReturnNoErrors_whenNotConsentedNotifiedApplicationIsBeingMade() {
        //
        //            GeneralApplicationCaseData caseData =  GeneralApplicationCaseDataBuilder.builder().buildFeeValidationCaseData(FEE108, false, false);
        //            when(feesService.getFeeForGA(caseData))
        //                .thenReturn(FEE108);
        //
        //            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        //            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //
        //            verify(feesService).getFeeForGA(caseData);
        //            assertThat(response.getErrors()).isEmpty();
        //        }

        @Test
        void returnsCorrectTaskId() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildFeeValidationCaseData(FEE108, false, false);
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo(TASK_ID);
        }

        @Test
        void returnsCorrectEvents() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildFeeValidationCaseData(FEE108, false, false);
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            assertThat(handler.handledEvents()).contains(VALIDATE_FEE_GASPEC);
        }

        @Test
        void shouldReturnNoErrors_whenLipCaseData() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().gaPbaDetails(GAPbaDetails.builder().build()).build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(feesService);
            assertThat(response.getErrors()).isEmpty();
        }
    }
}
