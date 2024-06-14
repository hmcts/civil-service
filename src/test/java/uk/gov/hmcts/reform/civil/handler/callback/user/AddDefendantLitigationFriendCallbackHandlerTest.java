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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    AddDefendantLitigationFriendCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
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

    @MockBean
    private CaseFlagsInitialiser caseFlagInitialiser;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Autowired
    private UserService userService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldSetSelectLitigationFriend_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddLitigationFriend_1v2_SameSolicitor()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertTrue(response.getData().containsKey("selectLitigationFriend"));
        }

        @Test
        void shouldNotSetIsRespondent1_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddLitigationFriend_1v2_SameSolicitor()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertFalse(response.getData().containsKey("isRespondent1"));
        }

        @Test
        void shouldNotSetSelectLitigationFriend_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddRespondent1LitigationFriend_1v2_DiffSolicitor()
                .build();

            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertFalse(response.getData().containsKey("selectLitigationFriend"));
        }

        @Test
        void shouldSetIsRespondent1ToYes_whenInvokedAsRespondent1Solicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddRespondent1LitigationFriend_1v2_DiffSolicitor()
                .build();

            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("Yes", response.getData().get("isRespondent1"));
        }

        @Test
        void shouldSetIsRespondent1ToYes_whenInvokedAsRespondent2Solicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddRespondent2LitigationFriend_1v2_DiffSolicitor()
                .respondent2Represented(YES)
                .respondent2OrgRegistered(YES)
                .build();

            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), any())).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("No", response.getData().get("isRespondent1"));
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

            assertThat(response.getData()).doesNotHaveToString("respondent1LitigationFriend");
            assertThat(response.getData()).doesNotHaveToString("respondent2LitigationFriend");

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
            assertThat(response.getData()).doesNotHaveToString("respondent2LitigationFriend");
            assertThat(response.getData()).extracting("caseNameHmctsInternal")
                .isEqualTo("'John Rambo' v 'Sole Trader' represented by 'Litigation Friend' (litigation friend), 'John Rambo'");
            assertThat(response.getData()).extracting("caseNamePublic")
                .isEqualTo("'John Rambo' v 'Sole Trader' represented by 'Litigation Friend' (litigation friend), 'John Rambo'");

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

            assertThat(response.getData()).doesNotHaveToString("respondent1LitigationFriend");
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriendDate").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriendCreatedDate").isNotNull();
            assertThat(response.getData()).extracting("caseNameHmctsInternal")
                .isEqualTo("'John Rambo' v 'Sole Trader', 'John Rambo' represented by 'Litigation Friend' (litigation friend)");
            assertThat(response.getData()).extracting("caseNamePublic")
                .isEqualTo("'John Rambo' v 'Sole Trader', 'John Rambo' represented by 'Litigation Friend' (litigation friend)");
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

            assertThat(response.getData()).doesNotHaveToString("respondent1LitigationFriend");
            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("caseNameHmctsInternal")
                .isEqualTo("'John Rambo' v 'Sole Trader', 'John Rambo' represented by "
                             + "'Litigation Friend' (litigation friend)");
            assertThat(response.getData()).extracting("caseNamePublic")
                .isEqualTo("'John Rambo' v 'Sole Trader', 'John Rambo' represented by "
                             + "'Litigation Friend' (litigation friend)");
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
            assertThat(response.getData()).doesNotHaveToString("respondent2LitigationFriend");
            assertThat(response.getData()).extracting("caseNameHmctsInternal")
                .isEqualTo("'John Rambo' v 'Sole Trader' represented by 'Litigation Friend' (litigation friend), 'John Rambo'");
            assertThat(response.getData()).extracting("caseNamePublic")
                .isEqualTo("'John Rambo' v 'Sole Trader' represented by 'Litigation Friend' (litigation friend), 'John Rambo'");

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
            assertThat(response.getData()).doesNotHaveToString("respondent2LitigationFriend");
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

        @Test
        void shouldSetRespondent1LF_WithMultiParty_1v2_DiffSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddRespondent1LitigationFriend_1v2_DiffSolicitor()
                .build();

            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent1LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent1LitigationFriendDate").isNotNull();
            assertThat(response.getData()).extracting("respondent1LitigationFriendCreatedDate").isNotNull();
            assertThat(response.getData()).doesNotHaveToString("respondent2LitigationFriend");
            assertThat(response.getData()).extracting("caseNameHmctsInternal")
                .isEqualTo("'John Rambo' v 'Sole Trader' represented by 'Litigation Friend' (litigation friend), 'John Rambo'");
        }

        @Test
        void shouldSetRespondent2LF_WithMultiParty_1v2_DiffSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddRespondent2LitigationFriend_1v2_DiffSolicitor()
                .build();

            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_DEFENDANT_LITIGATION_FRIEND.name(), "READY");

            assertThat(response.getData()).extracting("respondent2LitigationFriend").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriendDate").isNotNull();
            assertThat(response.getData()).extracting("respondent2LitigationFriendCreatedDate").isNotNull();
            assertThat(response.getData()).doesNotHaveToString("respondent1LitigationFriend");
            assertThat(response.getData()).extracting("caseNameHmctsInternal")
                .isEqualTo("'John Rambo' v 'Sole Trader', 'John Rambo' represented by 'Litigation Friend' (litigation friend)");
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
