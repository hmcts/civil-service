package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.OBTAIN_ADDITIONAL_FEE_VALUE;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;

@ExtendWith(MockitoExtension.class)
class AdditionalFeeValueCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    public static final String VERSION = "1";
    private static final Fee FEE167 = new Fee()
        .setCalculatedAmountInPence(BigDecimal.valueOf(16700))
        .setCode("FEE0444")
        .setVersion(VERSION);
    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS_167 = BigDecimal.valueOf(16700);
    public static final String TEST_FEE_CODE = "test_fee_code";
    public static final String SOME_EXCEPTION = "Some Exception";

    @InjectMocks
    private AdditionalFeeValueCallbackHandler handler;

    private static final String TASK_ID = "ObtainAdditionalFeeValue";

    @Mock
    private GeneralAppFeesService generalAppFeesService;

    @Mock
    private GeneralAppFeesConfiguration generalAppFeesConfiguration;

    private CallbackParams params;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Mock
    JudicialDecisionHelper judicialDecisionHelper;

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    @Test
     void shouldReturnCorrectTaskId() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildFeeValidationCaseData(FEE167, false, false);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.camundaActivityId(new CallbackParams())).isEqualTo(TASK_ID);
    }

    @Test
    void shouldReturnCorrectEvent() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildFeeValidationCaseData(FEE167, false, false);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(OBTAIN_ADDITIONAL_FEE_VALUE);
    }

    @Test
    void shouldReturnAdditionalFeeValue_WhenApplicationUncloaked() {
        when(generalAppFeesService.getFeeForGA(any(), any(), any()))
            .thenReturn(new Fee()
                            .setCalculatedAmountInPence(TEST_FEE_AMOUNT_POUNDS_167)
                            .setCode(TEST_FEE_CODE)
                            .setVersion(VERSION));

        var caseData = GeneralApplicationCaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(
                REQUEST_MORE_INFORMATION, YesOrNo.NO, YesOrNo.NO)
            .build();

        when(judicialDecisionHelper
                 .isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(true);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Fee expectedFeeDto = new Fee()
            .setCalculatedAmountInPence(TEST_FEE_AMOUNT_POUNDS_167)
            .setCode(TEST_FEE_CODE)
            .setVersion(VERSION);

        assertThat(extractAdditionalUncloakFee(response)).isEqualTo(expectedFeeDto);
    }

    @Test
    void shouldNotGetAdditionalFeeValue_WhenApplicationIsNotUncloaked() {

        var caseData = GeneralApplicationCaseDataBuilder.builder()
            .requestForInformationApplication()
            .build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        verify(generalAppFeesService, never()).getFeeForGA(any(), any(), any());
    }

    @Test
    void shouldSetAppplicationFeeAmount_WhenApplicationUncloaked() {
        when(generalAppFeesService.getFeeForGA(any(), any(), any()))
            .thenReturn(new Fee()
                            .setCalculatedAmountInPence(TEST_FEE_AMOUNT_POUNDS_167)
                            .setCode(TEST_FEE_CODE)
                            .setVersion(VERSION));

        var caseData = GeneralApplicationCaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(
                REQUEST_MORE_INFORMATION, YesOrNo.NO, YesOrNo.NO)
            .build();
        caseData = caseData.copy()
            .isGaApplicantLip(YesOrNo.YES)
            .build();

        when(judicialDecisionHelper
                 .isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(true);

        BigDecimal expectedApplicationFeeAmount = caseData.getGeneralAppPBADetails().getFee().getCalculatedAmountInPence();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(responseCaseData.getApplicationFeeAmountInPence()).isEqualTo(expectedApplicationFeeAmount);
    }

    @Test
    void shouldThrowError_whenRunTimeExceptionHappens() {

        when(generalAppFeesService.getFeeForGA(any(), any(), any()))
            .thenThrow(new RuntimeException(SOME_EXCEPTION));

        var caseData = GeneralApplicationCaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(
                REQUEST_MORE_INFORMATION, YesOrNo.NO, YesOrNo.NO)
            .build();

        when(judicialDecisionHelper
                 .isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(true);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        Exception exception = assertThrows(RuntimeException.class, () -> handler.handle(params));

        assertThat(exception.getMessage()).isEqualTo(SOME_EXCEPTION);
    }

    private Fee extractAdditionalUncloakFee(AboutToStartOrSubmitCallbackResponse response) {
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        if (responseCaseData.getGeneralAppPBADetails() != null
            && responseCaseData.getGeneralAppPBADetails().getFee() != null) {
            return responseCaseData.getGeneralAppPBADetails().getFee();
        }

        return null;
    }
}
