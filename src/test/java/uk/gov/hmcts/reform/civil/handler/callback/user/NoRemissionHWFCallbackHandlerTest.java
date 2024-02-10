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
import uk.gov.hmcts.reform.civil.enums.HWFFeeDetailsSummary;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class NoRemissionHWFCallbackHandlerTest extends BaseCallbackHandlerTest {

    private NoRemissionHWFCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new NoRemissionHWFCallbackHandler(new ObjectMapper());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NO_REMISSION_HWF);
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldUpdateNoRemissionData() {
            CaseData caseData = CaseData.builder()
                .hwFeesDetails(HelpWithFeesDetails.builder().noRemissionDetails("no remission")
                                   .noRemissionDetailsSummary(HWFFeeDetailsSummary.FEES_REQUIREMENT_NOT_MET).hwfFeeType(
                        FeeType.CLAIMISSUED).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("hwFeesDetails").isNotNull();
        }
    }
}
