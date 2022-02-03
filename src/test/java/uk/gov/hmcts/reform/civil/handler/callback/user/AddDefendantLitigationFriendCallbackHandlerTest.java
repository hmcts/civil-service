package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;

@SpringBootTest(classes = {
    AddDefendantLitigationFriendCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class
})
class AddDefendantLitigationFriendCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @Autowired
    private AddDefendantLitigationFriendCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPrepopulateDynamicListWithOptions_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddLitigationFriend_1v2_SameSolicitor()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertTrue(response.getData().containsKey("selectLitigationFriend"));
        }
    }

    @Nested
    class AboutToSubmitWithMultiPartyToggleOn {
        private LocalDateTime localDateTime;

        @BeforeEach
        private void setup() {
            when(time.now()).thenReturn(LocalDateTime.now());
        }

        @Test
        void shouldSetLitigantFriendFlag_whenInvoked_WithMultiParty_1v2_SameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .selectLitigationFriend("Defendant One: Mr. Def One")
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "get-litigation-friend");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("litigantFriendSelection").isEqualTo("DEFENDANT ONE");

        }

        @Test
        void shouldUpdateBusinessProcessToReadyWithEvent_whenInvoked_WithMultiParty_1v2_SameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateAddLitigationFriend_1v2_SameSolicitor()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent1LitigationFriend").isNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNull();

        }

        @Test
        void shouldSetRespondent1LF_WhenRespondentOneSelected_WithMultiParty_1v2_SameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddRespondent1LitigationFriend_1v2_SameSolicitor()
                .selectLitigationFriend("Defendant One: Mr. Def One")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent1LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent1LitigationFriendDate").isNotNull();
            assertThat(response.getData()).extracting("respondent1LitigationFriendCreatedDate").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNull();

        }

        @Test
        void shouldSetRespondent2LF_WhenRespondentTwoSelected_WithMultiParty_1v2_SameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddRespondent2LitigationFriend_1v2_SameSolicitor()
                .selectLitigationFriend("Defendant Two: Mr Def Two")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent1LitigationFriend").isNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriendDate").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriendCreatedDate").isNotNull();

        }

        @Test
        void shouldSetBothRespondentLF_WhenBothRespondentSelected_WithMultiParty_1v2_SameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddLitigationFriend_1v2_SameSolicitor()
                .selectLitigationFriend("Both")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent1LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent1LitigationFriendDate").isNotNull();
            assertThat(response.getData()).extracting("respondent1LitigationFriendCreatedDate").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriendDate").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriendCreatedDate").isNotNull();

        }

        @Test
        void shouldSetRespondent2_whenRespondent1OptionIsSelected_WithMultiParty_1v2_SameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddRespondent2LitigationFriend_1v2_SameSolicitor()
                .selectLitigationFriend("Respondent Two: Test Respondent 2")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent1LitigationFriend").isNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNotNull();

        }

        @Test
        void shouldSetRespondent2_whenRespondent2OptionIsSelected_WithMultiParty_1v2_SameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddRespondent1LitigationFriend_1v2_SameSolicitor()
                .selectLitigationFriend("Respondent One: Test Respondent 1")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent1LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNull();

        }

        @Test
        void shouldSetBothRespondent_whenRespondent2OptionIsSelected_WithMultiParty_1v2_SameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddLitigationFriend_1v2_SameSolicitor()
                .selectLitigationFriend("Both")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent1LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNotNull();

        }

        @Test
        void should_Set_Respondent1_Ensuring_That_Production_Is_Unaffected_1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent1LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNull();

        }

        @Test
        void shouldUpdateBusinessProcessToReadyWithEvent_WhenGenericRespondentLitigationFriendIsSet() {
            CaseData caseData = CaseDataBuilder.builder().addGenericRespondentLitigationFriend()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

        }

    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# You have added litigation friend details")
                    .confirmationBody(exitSurveyContentService.respondentSurvey())
                    .build());
        }
    }
}
