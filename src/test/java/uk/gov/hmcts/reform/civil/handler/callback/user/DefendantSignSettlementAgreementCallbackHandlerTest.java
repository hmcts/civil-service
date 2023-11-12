package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_SIGN_SETTLEMENT_AGREEMENT;

@ExtendWith(MockitoExtension.class)
public class DefendantSignSettlementAgreementCallbackHandlerTest extends BaseCallbackHandlerTest {

    private DefendantSignSettlementAgreementCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DefendantSignSettlementAgreementCallbackHandler(new ObjectMapper());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseDataLip(CaseDataLiP.builder()
                            .respondentSignSettlementAgreement(YesOrNo.NO)
                            .build())
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUpdateBusinessProcess() {
            CaseData caseData = CaseDataBuilder.builder()
                    .caseDataLip(CaseDataLiP.builder()
                            .respondentSignSettlementAgreement(YesOrNo.NO)
                            .build())
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                    .extracting("businessProcess")
                    .extracting("camundaEvent")
                    .isEqualTo(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT.name());
            assertThat(response.getData())
                    .extracting("businessProcess")
                    .extracting("status")
                    .isEqualTo("READY");
        }
    }

}
