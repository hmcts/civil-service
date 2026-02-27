package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.user.GaPartialRemissionHWFCallbackHandler.ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ADDITIONAL_FEE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.user.GaPartialRemissionHWFCallbackHandler.ERR_MSG_REMISSION_AMOUNT_LESS_THAN_GA_FEE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.user.GaPartialRemissionHWFCallbackHandler.ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ZERO;

@ExtendWith(MockitoExtension.class)
public class GaPartialRemissionHWFCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private ObjectMapper objectMapper;
    private GaPartialRemissionHWFCallbackHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new GaPartialRemissionHWFCallbackHandler(objectMapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(PARTIAL_REMISSION_HWF_GA);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCallPartialRemissionHwfEventWhenFeeTypeIsGa() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                    .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                    new Fee()
                                            .setCalculatedAmountInPence(BigDecimal.valueOf(10000)).setCode("OOOCM002"))
                            )
                    .gaHwfDetails(new HelpWithFeesDetails()
                            .setRemissionAmount(BigDecimal.valueOf(1000))
                            .setHwfCaseEvent(PARTIAL_REMISSION_HWF_GA)
                            )
                    .hwfFeeType(FeeType.APPLICATION)
                    .build();

            CallbackParams params = callbackParamsOf(caseData, CaseEvent.PARTIAL_REMISSION_HWF_GA, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then
            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(response.getErrors()).isNull();
            assertThat(updatedData.getGaHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(1000));
            assertThat(updatedData.getGaHwfDetails().getHwfCaseEvent()).isEqualTo(PARTIAL_REMISSION_HWF_GA);
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(NOTIFY_APPLICANT_LIP_HWF.toString());
        }

        @Test
        void shouldCallPartialRemissionHwfEventWhenFeeTypeIsHearing() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                    .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                    new Fee()
                                            .setCalculatedAmountInPence(BigDecimal.valueOf(10000)).setCode("OOOCM002"))
                            )
                    .additionalHwfDetails(new HelpWithFeesDetails()
                            .setRemissionAmount(BigDecimal.valueOf(1000))
                            .setHwfCaseEvent(PARTIAL_REMISSION_HWF_GA)
                            )
                    .hwfFeeType(FeeType.ADDITIONAL)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, CaseEvent.PARTIAL_REMISSION_HWF_GA, CallbackType.ABOUT_TO_SUBMIT);

            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then
            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(response.getErrors()).isNull();
            assertThat(updatedData.getAdditionalHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(1000));
            assertThat(updatedData.getAdditionalHwfDetails().getHwfCaseEvent()).isEqualTo(PARTIAL_REMISSION_HWF_GA);
        }
    }

    @Test
    void shouldPopulateErrorWhenApplicationRemissionAmountIsNegative() {
        //Given
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                new Fee()
                                        .setCalculatedAmountInPence(BigDecimal.valueOf(30000))
                                        .setCode("OOOCM002"))
                        )
                .gaHwfDetails(new HelpWithFeesDetails()
                        .setRemissionAmount(BigDecimal.valueOf(-1000))
                        )
                .hwfFeeType(FeeType.APPLICATION)
                .build();

        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-amount");

        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        assertThat(response.getErrors()).containsExactly(ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ZERO);
    }

    @Test
    void shouldPopulateErrorWhenAdditionalRemissionAmountIsNegative() {
        //Given
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                new Fee()
                                        .setCalculatedAmountInPence(BigDecimal.valueOf(30000))
                                        .setCode("OOOCM002"))
                        )
                .additionalHwfDetails(new HelpWithFeesDetails()
                        .setRemissionAmount(BigDecimal.valueOf(-1000))
                        )
                .hwfFeeType(FeeType.ADDITIONAL)

                .build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-amount");

        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        assertThat(response.getErrors()).containsExactly(ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ZERO);
    }

    @ParameterizedTest
    @MethodSource("provideFeeTypes")
    void shouldPopulateErrorWhenRemissionAmountIsNotValidForDifferentFeeTypes(FeeType feeType, String errMsg) {
        //Given
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                new Fee()
                                        .setCalculatedAmountInPence(BigDecimal.valueOf(30000))
                                        .setCode("OOOCM002"))
                        )
                .additionalHwfDetails(new HelpWithFeesDetails()
                        .setRemissionAmount(BigDecimal.valueOf(35000))
                        )
                .gaHwfDetails(new HelpWithFeesDetails()
                        .setRemissionAmount(BigDecimal.valueOf(35000))
                        )
                .hwfFeeType(feeType)
                .build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-amount");

        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        assertThat(response.getErrors()).containsExactly(errMsg);
    }

    private static Stream<Arguments> provideFeeTypes() {
        return Stream.of(
                Arguments.of(FeeType.APPLICATION, ERR_MSG_REMISSION_AMOUNT_LESS_THAN_GA_FEE),
                Arguments.of(FeeType.ADDITIONAL, ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ADDITIONAL_FEE)
        );
    }

}
