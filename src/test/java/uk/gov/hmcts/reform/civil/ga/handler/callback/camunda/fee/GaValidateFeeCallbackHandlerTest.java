package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.fee;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VALIDATE_FEE_GASPEC;

@ExtendWith(MockitoExtension.class)
class GaValidateFeeCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    public static final String VERSION = "1";
    private static final Fee FEE108 = new Fee().setCalculatedAmountInPence(
        BigDecimal.valueOf(10800)).setCode("FEE0443").setVersion(VERSION);
    private static final String TASK_ID = "GeneralApplicationValidateFee";

    @InjectMocks
    private GaValidateFeeCallbackHandler handler;
    private CallbackParams params;

    @Nested
    class MakePBAPayments {

        @Test
        void returnsCorrectTaskId() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildFeeValidationCaseData(FEE108, false, false);
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            assertThat(handler.camundaActivityId(new CallbackParams())).isEqualTo(TASK_ID);
        }

        @Test
        void returnsCorrectEvents() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildFeeValidationCaseData(FEE108, false, false);
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            assertThat(handler.handledEvents()).contains(VALIDATE_FEE_GASPEC);
        }

        @Test
        void shouldReturnNoErrors() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().gaPbaDetails(
                new GeneralApplicationPbaDetails()).build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }
}
