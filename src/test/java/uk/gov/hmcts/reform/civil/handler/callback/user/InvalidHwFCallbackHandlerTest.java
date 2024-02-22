package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME;

@ExtendWith(MockitoExtension.class)
public class InvalidHwFCallbackHandlerTest extends BaseCallbackHandlerTest {

    private InvalidHwFCallbackHandler handler;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new InvalidHwFCallbackHandler(objectMapper);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldHandleInvalidRefNumberSuccessfully_FeeType_ClaimIssued() {
            //Given
            CaseData caseData = CaseData.builder()
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder().build())
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData data = getCaseData(response);
            assertThat(data.getBusinessProcess().getCamundaEvent()).isEqualTo(NOTIFY_LIP_CLAIMANT_HWF_OUTCOME.toString());
            assertThat(data.getClaimIssuedHwfDetails().getHwfCaseEvent()).isEqualTo(INVALID_HWF_REFERENCE);
        }

        @Test
        void shouldHandleInvalidRefNumberSuccessfully_FeeType_Hearing() {
            //Given
            CaseData caseData = CaseData.builder()
                .hwfFeeType(FeeType.HEARING)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData data = getCaseData(response);
            assertThat(data.getBusinessProcess().getCamundaEvent()).isEqualTo(NOTIFY_LIP_CLAIMANT_HWF_OUTCOME.toString());
            assertThat(data.getHearingHwfDetails().getHwfCaseEvent()).isEqualTo(INVALID_HWF_REFERENCE);
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(INVALID_HWF_REFERENCE);
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

}
