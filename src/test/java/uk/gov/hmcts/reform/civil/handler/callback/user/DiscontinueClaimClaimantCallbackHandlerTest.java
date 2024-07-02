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
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM_CLAIMANT;

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
        void should_return_error_if_error_list_present() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atState1v2DifferentSolicitorClaimDetailsRespondent1NotifiedTimeExtension().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response.getErrors()).isNotNull();
        }

        @Test
        void shouldPopulateClaimantList_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("claimantWhoIsDiscontinuing")).isNotNull();
        }

        @Test
        void shouldPopulateDefendantListFor1v2LrVLr_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("Resp1").build())
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).partyName("Resp2").build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("discontinuingAgainstOneDefendant")).isNotNull();
        }

        @Test
        void shouldNotPopulateDefendantListFor2v1LrVLr_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("discontinuingAgainstOneDefendant")).isNull();
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

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(DISCONTINUE_CLAIM_CLAIMANT);
    }
}
