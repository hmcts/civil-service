package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;

@SpringBootTest(classes = {
    DiscontinueClaimClaimantCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class DiscontinueClaimClaimantCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DiscontinueClaimClaimantCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNullClaimantList_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("claimantWhoIsDiscontinuing")).isNull();
        }

        @Test
        void shouldPopulateClaimantList_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("claimantWhoIsDiscontinuing")).isNotNull();
        }
    }

    @Nested
    class MidEventCheckIfConsentRequiredCallback {

        private static final String PAGE_ID = "showClaimantConsent";

        @Test
        void shouldSetSelectedClaimant_when2v1() {
            DynamicList claimantWhoIsDiscontinuingList = DynamicList.builder()
                .value(DynamicListElement.builder()
                           .label("Both")
                           .build())
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            caseData.setClaimantWhoIsDiscontinuing(claimantWhoIsDiscontinuingList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("selectedClaimantForDiscontinuance")).isNotNull();
        }

        @Test
        void shouldNotPopulateSelectedClaimant_whenClaimNot2v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("selectedClaimantForDiscontinuance")).isNull();
        }
    }
}
