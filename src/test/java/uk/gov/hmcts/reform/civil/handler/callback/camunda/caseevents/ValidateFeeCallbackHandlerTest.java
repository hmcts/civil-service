package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class ValidateFeeCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String ERROR_MESSAGE = "Fee has changed since claim submitted. It needs to be validated again";

    @InjectMocks
    private ValidateFeeCallbackHandler handler;

    @Mock
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

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("ValidateClaimFee");
    }
}
