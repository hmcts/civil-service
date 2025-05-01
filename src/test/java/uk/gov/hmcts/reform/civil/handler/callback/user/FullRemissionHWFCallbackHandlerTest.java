package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.math.BigDecimal;
import uk.gov.hmcts.reform.civil.service.citizenui.HelpWithFeesForTabService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class FullRemissionHWFCallbackHandlerTest extends BaseCallbackHandlerTest {

    private FullRemissionHWFCallbackHandler handler;
    @Mock
    private HelpWithFeesForTabService hwfForTabService;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        handler = new FullRemissionHWFCallbackHandler(mapper, hwfForTabService);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(FULL_REMISSION_HWF);
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldUpdateFullRemissionData_ClaimFee() {
            CaseData caseData = CaseData.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder().build())
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getClaimIssuedHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(10000));
            assertThat(updatedData.getClaimIssuedHwfDetails().getHwfCaseEvent()).isEqualTo(FULL_REMISSION_HWF);
        }

        @Test
        void shouldUpdateFullRemissionDataWithDetailsNull_ClaimFee() {
            CaseData caseData = CaseData.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getClaimIssuedHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(10000));
            assertThat(updatedData.getClaimIssuedHwfDetails().getHwfCaseEvent()).isEqualTo(FULL_REMISSION_HWF);
        }

        @Test
        void shouldUpdateFullRemissionData_HearingFee() {
            CaseData caseData = CaseData.builder()
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingHwfDetails(HelpWithFeesDetails.builder().build())
                .hwfFeeType(FeeType.HEARING)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHearingHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(30000));
            assertThat(updatedData.getHearingHwfDetails().getHwfCaseEvent()).isEqualTo(FULL_REMISSION_HWF);
        }

        @Test
        void shouldUpdateFullRemissionDataWithDetailsNull_HearingFee() {
            CaseData caseData = CaseData.builder()
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hwfFeeType(FeeType.HEARING)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHearingHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(30000));
            assertThat(updatedData.getHearingHwfDetails().getHwfCaseEvent()).isEqualTo(FULL_REMISSION_HWF);
        }

        @Test
        void shouldNotUpdateFullRemissionData_ifClaimFeeIsZero() {
            CaseData caseData = CaseData.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.ZERO).code("OOOCM002").build())
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder().build())
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getClaimIssuedHwfDetails().getRemissionAmount()).isNull();
        }

        @Test
        void shouldNotUpdateFullRemissionData_ifHearingFeeIsZero() {
            CaseData caseData = CaseData.builder()
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.ZERO).build())
                .hearingHwfDetails(HelpWithFeesDetails.builder().build())
                .hwfFeeType(FeeType.HEARING)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHearingHwfDetails().getRemissionAmount()).isNull();
        }

    }
}
