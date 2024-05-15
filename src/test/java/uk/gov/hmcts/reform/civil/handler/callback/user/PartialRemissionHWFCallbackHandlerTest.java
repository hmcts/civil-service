package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.service.citizen.HWFFeePaymentOutcomeService;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.PartialRemissionHWFCallbackHandler.ERR_MSG_FEE_TYPE_NOT_CONFIGURED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.PartialRemissionHWFCallbackHandler.ERR_MSG_REMISSION_AMOUNT_LESS_THAN_CLAIM_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.PartialRemissionHWFCallbackHandler.ERR_MSG_REMISSION_AMOUNT_LESS_THAN_HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.PartialRemissionHWFCallbackHandler.ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ZERO;

@ExtendWith(MockitoExtension.class)
public class PartialRemissionHWFCallbackHandlerTest extends BaseCallbackHandlerTest {

    private ObjectMapper objectMapper;
    private PartialRemissionHWFCallbackHandler handler;
    @Mock
    private HWFFeePaymentOutcomeService hwfFeePaymentOutcomeService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new PartialRemissionHWFCallbackHandler(objectMapper, hwfFeePaymentOutcomeService);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(PARTIAL_REMISSION_HWF_GRANTED);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCallPartialRemissionHwfEventWhenFeeTypeIsClaimIssued() {
            CaseData caseData = CaseData.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .remissionAmount(BigDecimal.valueOf(1000))
                                           .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED)
                                           .build())
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            when(hwfFeePaymentOutcomeService.updateOutstandingFee(any(), any())).thenReturn(caseData);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getErrors()).isNull();
            assertThat(updatedData.getClaimIssuedHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(1000));
            assertThat(updatedData.getClaimIssuedHwfDetails().getHwfCaseEvent()).isEqualTo(PARTIAL_REMISSION_HWF_GRANTED);
        }

        @Test
        void shouldCallPartialRemissionHwfEventWhenFeeTypeIsHearing() {
            CaseData caseData = CaseData.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
                .hearingHwfDetails(HelpWithFeesDetails.builder()
                                       .remissionAmount(BigDecimal.valueOf(1000))
                                       .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED)
                                       .build())
                .hwfFeeType(FeeType.HEARING)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

            //When
            when(hwfFeePaymentOutcomeService.updateOutstandingFee(any(), any())).thenReturn(caseData);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getErrors()).isNull();
            assertThat(updatedData.getHearingHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(1000));
            assertThat(updatedData.getHearingHwfDetails().getHwfCaseEvent()).isEqualTo(PARTIAL_REMISSION_HWF_GRANTED);
        }
    }

    @Test
    void shouldPopulateErrorWhenClaimIssuedRemissionAmountIsNegative() {
        //Given
        CaseData caseData = CaseData.builder()
            .hearingReferenceNumber("000HN001")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                       .remissionAmount(BigDecimal.valueOf(-1000))
                                       .build())
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "remission-amount");

        //When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        assertThat(response.getErrors()).containsExactly(ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ZERO);
    }

    @Test
    void shouldPopulateErrorWhenHearingRemissionAmountIsNegative() {
        //Given
        CaseData caseData = CaseData.builder()
            .hearingReferenceNumber("000HN001")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                       .remissionAmount(BigDecimal.valueOf(-1000))
                                       .build())
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .remissionAmount(BigDecimal.valueOf(-1000))
                                   .build())
            .hwfFeeType(FeeType.CLAIMISSUED)

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
        CaseData caseData = CaseData.builder()
            .hearingReferenceNumber("000HN001")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .remissionAmount(BigDecimal.valueOf(35000))
                                   .build())
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
            Arguments.of(FeeType.CLAIMISSUED, ERR_MSG_REMISSION_AMOUNT_LESS_THAN_CLAIM_FEE),
            Arguments.of(FeeType.HEARING, ERR_MSG_REMISSION_AMOUNT_LESS_THAN_HEARING_FEE),
            Arguments.of(null, ERR_MSG_FEE_TYPE_NOT_CONFIGURED)
        );
    }

}
