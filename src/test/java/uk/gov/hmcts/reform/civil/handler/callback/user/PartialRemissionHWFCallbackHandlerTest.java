package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;

@ExtendWith(MockitoExtension.class)
public class PartialRemissionHWFCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PartialRemissionHWFCallbackHandler handler;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(PARTIAL_REMISSION_HWF_GRANTED);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUpdatePartialRemissionHwfGranted() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                                 .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                            .applicant1SignedSettlementAgreement(YesOrNo.YES).build()
                                 )
                                 .build())
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        }

    }
}
