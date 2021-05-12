package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeesService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    ValidateFeeCallbackHandler.class,
    FeesService.class
})
class ValidateFeeCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String ERROR_MESSAGE = "Fee has changed since claim submitted. It needs to be validated again";

    @Autowired
    private ValidateFeeCallbackHandler handler;

    @MockBean
    private FeesService feesService;

    @Test
    void shouldReturnErrors_whenInitialFeeSsDifferentAfterReCalculation() {
        when(feesService.getFeeDataByClaimValue(any(ClaimValue.class)))
            .thenReturn(Fee.builder()
                            .calculatedAmountInPence(BigDecimal.valueOf(25))
                            .version("3")
                            .code("CODE2")
                            .build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNotEmpty().contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnErrors_whenInitialFeeIsSameAfterReCalculation() {
        when(feesService.getFeeDataByClaimValue(any(ClaimValue.class)))
            .thenReturn(Fee.builder()
                            .calculatedAmountInPence(BigDecimal.valueOf(100))
                            .version("1")
                            .code("CODE")
                            .build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isEmpty();
    }
}
