package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class FullRemissionHWFCallbackHandlerTest extends BaseCallbackHandlerTest {

    private FullRemissionHWFCallbackHandler handler;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new FullRemissionHWFCallbackHandler(mapper);
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
                .hwFeesDetails(HelpWithFeesDetails.builder().hwfFeeType(FeeType.CLAIMISSUED).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHwFeesDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(10000));
            assertThat(updatedData.getCalculatedClaimFeeInPence()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void shouldUpdateFullRemissionData_HearingFee() {
            CaseData caseData = CaseData.builder()
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hwFeesDetails(HelpWithFeesDetails.builder().hwfFeeType(FeeType.HEARING).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHwFeesDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(30000));
            assertThat(updatedData.getCalculatedHearingFeeInPence()).isEqualTo(BigDecimal.ZERO);
        }
    }
}
