package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.PartialRemissionHWFCallbackHandler.ERR_MSG_FEE_TYPE_NOT_CONFIGURED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.PartialRemissionHWFCallbackHandler.ERR_MSG_REMISSION_AMOUNT_LESS_THAN_CLAIM_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.PartialRemissionHWFCallbackHandler.ERR_MSG_REMISSION_AMOUNT_LESS_THAN_HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.PartialRemissionHWFCallbackHandler.ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ZERO;

@ExtendWith(MockitoExtension.class)
public class PartialRemissionHWFCallbackHandlerTest extends BaseCallbackHandlerTest {

    private ObjectMapper objectMapper;
    private PartialRemissionHWFCallbackHandler handler;
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new PartialRemissionHWFCallbackHandler(objectMapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(PARTIAL_REMISSION_HWF_GRANTED);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUpdatePartialRemissionHwfClaimFeeWhenHwfFeeTypeIsClaimIssued() {
            CaseData caseData = CaseData.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
                .hwFeesDetails(HelpWithFeesDetails.builder().hwfFeeType(FeeType.CLAIMISSUED)
                                   .remissionAmount(BigDecimal.valueOf(4000))
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHwFeesDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(4000));
            assertThat(updatedData.getCalculatedClaimFeeInPence()).isEqualTo(BigDecimal.valueOf(6000));
        }

        @Test
        void shouldUpdatePartialRemissionHwfHearingFeeWhenHwfFeeTypeIsHearing() {
           //Given
            CaseData caseData = CaseData.builder()
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hwFeesDetails(HelpWithFeesDetails.builder().hwfFeeType(FeeType.HEARING)
                                   .remissionAmount(BigDecimal.valueOf(4000))
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHwFeesDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(4000));
            assertThat(updatedData.getHearingFeeAmount()).isEqualTo(BigDecimal.valueOf(26000));
        }

    }

    @Test
    void shouldPopulateErrorWhenRemissionAmountIsNegative() {
        //Given
        CaseData caseData = CaseData.builder()
            .hearingReferenceNumber("000HN001")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hwFeesDetails(HelpWithFeesDetails.builder().hwfFeeType(FeeType.CLAIMISSUED)
                               .remissionAmount(BigDecimal.valueOf(-1000))
                               .build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-amount");

        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        var errors = response.getErrors();
        assertTrue(errors.contains(ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ZERO));
    }

    @ParameterizedTest
    @MethodSource("provideFeeTypes")
    void shouldPopulateErrorWhenRemissionAmountIsNotValidForDifferentFeeTypes(FeeType feeType, String errMsg ) {
        //Given
        CaseData caseData = CaseData.builder()
            .hearingReferenceNumber("000HN001")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hwFeesDetails(HelpWithFeesDetails.builder().hwfFeeType(feeType)
                               .remissionAmount(BigDecimal.valueOf(35000))
                               .build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-amount");

        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        var errors = response.getErrors();
        assertTrue(errors.contains(errMsg));
    }
    private static Stream<Arguments> provideFeeTypes() {
        return Stream.of(
            Arguments.of(FeeType.CLAIMISSUED, ERR_MSG_REMISSION_AMOUNT_LESS_THAN_CLAIM_FEE),
            Arguments.of(FeeType.HEARING, ERR_MSG_REMISSION_AMOUNT_LESS_THAN_HEARING_FEE),
            Arguments.of(null, ERR_MSG_FEE_TYPE_NOT_CONFIGURED)
            );
    }

}
