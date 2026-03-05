package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF_GA;

@ExtendWith(MockitoExtension.class)
public class GaFullRemissionHWFCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private GaFullRemissionHWFCallbackHandler handler;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        handler = new GaFullRemissionHWFCallbackHandler(mapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(FULL_REMISSION_HWF_GA);
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldUpdateFullRemissionData_GaFee() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                        new Fee()
                            .setCalculatedAmountInPence(BigDecimal.valueOf(10000)).setCode("OOOCM002"))
                        )
                .gaHwfDetails(new HelpWithFeesDetails())
                .hwfFeeType(FeeType.APPLICATION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getGaHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(10000));
            assertThat(updatedData.getGaHwfDetails().getHwfCaseEvent()).isEqualTo(FULL_REMISSION_HWF_GA);
        }

        @Test
        void shouldUpdateFullRemissionDataWithDetailsNull_GaFee() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                    new Fee()
                        .setCalculatedAmountInPence(BigDecimal.valueOf(10000)).setCode("OOOCM002"))
                    )
                .gaHwfDetails(new HelpWithFeesDetails())
                .hwfFeeType(FeeType.APPLICATION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getGaHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(10000));
            assertThat(updatedData.getGaHwfDetails().getHwfCaseEvent()).isEqualTo(FULL_REMISSION_HWF_GA);
        }

        @Test
        void shouldUpdateFullRemissionData_Additional() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                    .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                    new Fee()
                                            .setCalculatedAmountInPence(BigDecimal.valueOf(30000))
                                            .setCode("OOOCM002"))
                            )
                .additionalHwfDetails(new HelpWithFeesDetails())
                .hwfFeeType(FeeType.ADDITIONAL)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getAdditionalHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(30000));
            assertThat(updatedData.getAdditionalHwfDetails().getHwfCaseEvent()).isEqualTo(FULL_REMISSION_HWF_GA);
        }

        @Test
        void shouldUpdateFullRemissionDataWithDetailsNull_AdditionalFee() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                    .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                    new Fee()
                                            .setCalculatedAmountInPence(BigDecimal.valueOf(30000))
                                            .setCode("OOOCM002"))
                            )
                .hwfFeeType(FeeType.ADDITIONAL)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getAdditionalHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(30000));
            assertThat(updatedData.getAdditionalHwfDetails().getHwfCaseEvent()).isEqualTo(FULL_REMISSION_HWF_GA);
        }

        @Test
        void shouldNotUpdateFullRemissionData_ifGaFeeIsZero() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                    .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                    new Fee()
                                            .setCalculatedAmountInPence(BigDecimal.ZERO)
                                            .setCode("FREE"))
                            )
                .gaHwfDetails(new HelpWithFeesDetails())
                .hwfFeeType(FeeType.APPLICATION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getGaHwfDetails().getRemissionAmount()).isNull();
        }

        @Test
        void shouldNotUpdateFullRemissionData_ifAdditionalFeeIsZero() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                new Fee()
                                        .setCalculatedAmountInPence(BigDecimal.ZERO)
                                        .setCode("FREE"))
                        )
                .additionalHwfDetails(new HelpWithFeesDetails())
                .hwfFeeType(FeeType.ADDITIONAL)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getAdditionalHwfDetails().getRemissionAmount()).isNull();
        }

    }
}
