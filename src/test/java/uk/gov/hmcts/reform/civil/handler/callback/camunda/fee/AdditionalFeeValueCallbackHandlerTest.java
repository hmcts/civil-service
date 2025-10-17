package uk.gov.hmcts.reform.civil.handler.callback.camunda.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.JudicialDecisionHelper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.OBTAIN_ADDITIONAL_FEE_VALUE;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;

@SpringBootTest(classes = {
    AdditionalFeeValueCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
})
class AdditionalFeeValueCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String VERSION = "1";
    private static final Fee FEE167 = Fee.builder().calculatedAmountInPence(
        BigDecimal.valueOf(16700)).code("FEE0444").version(VERSION).build();
    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS_167 = BigDecimal.valueOf(16700);
    public static final String TEST_FEE_CODE = "test_fee_code";
    public static final String SOME_EXCEPTION = "Some Exception";
    @Autowired
    private AdditionalFeeValueCallbackHandler handler;
    private static final String TASK_ID = "ObtainAdditionalFeeValue";
    @MockBean
    private GeneralAppFeesService generalAppFeesService;
    @MockBean
    GeneralAppFeesConfiguration generalAppFeesConfiguration;
    private CallbackParams params;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    JudicialDecisionHelper judicialDecisionHelper;
    @MockBean
    FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(generalAppFeesConfiguration.getApplicationUncloakAdditionalFee())
            .thenReturn(TEST_FEE_CODE);
    }

    @Test
     void shouldReturnCorrectTaskId() {
        CaseData caseData = CaseDataBuilder.builder().buildFeeValidationCaseData(FEE167, false, false);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.camundaActivityId()).isEqualTo(TASK_ID);
    }

    @Test
    void shouldReturnCorrectEvent() {
        CaseData caseData = CaseDataBuilder.builder().buildFeeValidationCaseData(FEE167, false, false);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(OBTAIN_ADDITIONAL_FEE_VALUE);
    }

    @Test
    void shouldReturnAdditionalFeeValue_WhenApplicationUncloaked() {
        when(generalAppFeesService.getFeeForGA(any(), any(), any()))
            .thenReturn(Fee.builder().calculatedAmountInPence(
                TEST_FEE_AMOUNT_POUNDS_167).code(TEST_FEE_CODE).version(VERSION).build());

        var caseData = CaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(
                REQUEST_MORE_INFORMATION, YesOrNo.NO, YesOrNo.NO)
            .build();

        when(judicialDecisionHelper
                 .isApplicationUncloakedWithAdditionalFee(any(CaseData.class))).thenReturn(true);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Fee expectedFeeDto = Fee.builder()
                .calculatedAmountInPence(TEST_FEE_AMOUNT_POUNDS_167)
                .code(TEST_FEE_CODE)
                .version(VERSION)
                .build();

        assertThat(extractAdditionalUncloakFee(response)).isEqualTo(expectedFeeDto);
    }

    @Test
    void shouldNotGetAdditionalFeeValue_WhenApplicationIsNotUncloaked() {
        when(generalAppFeesService.getFeeForGA(any(), any(), any()))
            .thenReturn(Fee.builder().calculatedAmountInPence(
                BigDecimal.valueOf(16700)).code("").version(VERSION).build());

        var caseData = CaseDataBuilder.builder()
            .requestForInformationApplication()
            .build();
        when(judicialDecisionHelper
                 .isApplicationUncloakedWithAdditionalFee(any(CaseData.class))).thenReturn(false);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);
        verify(generalAppFeesService, never()).getFeeForGA(any(), any(), any());
    }

    @Test
    void shouldSetAppplicationFeeAmount_WhenApplicationUncloaked() {
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(generalAppFeesService.getFeeForGA(any(), any(), any()))
            .thenReturn(Fee.builder().calculatedAmountInPence(
                TEST_FEE_AMOUNT_POUNDS_167).code(TEST_FEE_CODE).version(VERSION).build());

        var caseData = CaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(
                REQUEST_MORE_INFORMATION, YesOrNo.NO, YesOrNo.NO)
            .build();
        caseData = caseData.toBuilder()
            .isGaApplicantLip(YesOrNo.YES)
            .build();

        when(judicialDecisionHelper
                 .isApplicationUncloakedWithAdditionalFee(any(CaseData.class))).thenReturn(true);

        BigDecimal expectedApplicationFeeAmount = caseData.getGeneralAppPBADetails().getFee().getCalculatedAmountInPence();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(responseCaseData.getApplicationFeeAmountInPence()).isEqualTo(expectedApplicationFeeAmount);
    }

    @Test
    void shouldThrowError_whenRunTimeExceptionHappens() {

        when(generalAppFeesService.getFeeForGA(any(), any(), any()))
            .thenThrow(new RuntimeException(SOME_EXCEPTION));

        var caseData = CaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(
                REQUEST_MORE_INFORMATION, YesOrNo.NO, YesOrNo.NO)
            .build();

        when(judicialDecisionHelper
                 .isApplicationUncloakedWithAdditionalFee(any(CaseData.class))).thenReturn(true);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        Exception exception = assertThrows(RuntimeException.class, () -> handler.handle(params));

        assertThat(exception.getMessage()).isEqualTo(SOME_EXCEPTION);
    }

    @Test
    void shouldReturnAdditionalFeeValue_whenGaCaseDataOnlyProvided() {
        when(generalAppFeesService.getFeeForGA(any(), any(), any()))
            .thenReturn(Fee.builder()
                .calculatedAmountInPence(TEST_FEE_AMOUNT_POUNDS_167)
                .code(TEST_FEE_CODE)
                .version(VERSION)
                .build());

        CaseData caseData = CaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(
                REQUEST_MORE_INFORMATION, YesOrNo.NO, YesOrNo.NO)
            .build();
        GeneralApplicationCaseData gaCaseData = toGaCaseData(caseData);

        when(judicialDecisionHelper
                 .isApplicationUncloakedWithAdditionalFee(any(CaseData.class))).thenReturn(true);

        CallbackParams params = gaCallbackParamsOf(gaCaseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Fee expectedFeeDto = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_POUNDS_167)
            .code(TEST_FEE_CODE)
            .version(VERSION)
            .build();

        assertThat(extractAdditionalUncloakFee(response)).isEqualTo(expectedFeeDto);
    }

    private Fee extractAdditionalUncloakFee(AboutToStartOrSubmitCallbackResponse response) {
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        if (responseCaseData.getGeneralAppPBADetails() != null
            && responseCaseData.getGeneralAppPBADetails().getFee() != null) {
            return responseCaseData.getGeneralAppPBADetails().getFee();
        }

        return null;
    }
}
