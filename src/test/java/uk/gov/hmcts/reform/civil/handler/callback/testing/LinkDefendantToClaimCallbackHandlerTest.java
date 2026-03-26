package uk.gov.hmcts.reform.civil.handler.callback.testing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LINK_DEFENDANT_TO_CLAIM;

@ExtendWith(MockitoExtension.class)
class LinkDefendantToClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String DEFENDANT_EMAIL = "valid@example.com";

    @Mock
    private ValidateEmailService validateEmailService;

    @Mock
    private IdamClient idamClient;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private UserService userService;

    @Mock
    private SystemUpdateUserConfiguration systemUpdateUserConfiguration;

    @Mock
    private FeatureToggleService featureToggleService;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @InjectMocks
    private LinkDefendantToClaimCallbackHandler handler;

    @Test
    void shouldReturnCorrectEvents_WhenHandledEventsCalledAndEnabled() {
        when(featureToggleService.isLinkDefendantTestingEnabled()).thenReturn(true);
        assertThat(handler.handledEvents()).containsOnly(LINK_DEFENDANT_TO_CLAIM);
    }

    @Test
    void shouldReturnEmptyList_WhenHandledEventsCalledAndDisabled() {
        when(featureToggleService.isLinkDefendantTestingEnabled()).thenReturn(false);
        assertThat(handler.handledEvents()).isEmpty();
    }

    @Test
    void shouldReturnEmptyResponse_WhenHandlerCalledAndDisabled_ForAboutToSubmit() {
        when(featureToggleService.isLinkDefendantTestingEnabled()).thenReturn(false);
        CallbackParams params = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, CaseDataBuilder.builder().build()).build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnEmptyResponse_WhenHandlerCalledAndDisabled_ForMidEvent() {
        when(featureToggleService.isLinkDefendantTestingEnabled()).thenReturn(false);
        CallbackParams params = CallbackParamsBuilder.builder()
            .of(MID, CaseDataBuilder.builder().build()).pageId("confirm-defendant-email").build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Nested
    class MidEventTests {

        public static final String PAGE_ID = "confirm-defendant-email";

        @BeforeEach
        void setUp() {
            when(featureToggleService.isLinkDefendantTestingEnabled()).thenReturn(true);
        }

        @Test
        void shouldValidateDefendantEmail_AndReturnNoErrorForValidEmail() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            caseData.setDefendantEmailAddress(DEFENDANT_EMAIL);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(MID, caseData)
                .pageId(PAGE_ID).build();

            when(validateEmailService.validate(DEFENDANT_EMAIL)).thenReturn(emptyList());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldValidateDefendantEmail_AndReturnErrorForInvalidEmail() {
            String defendantEmail = "invalid@example";
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            caseData.setDefendantEmailAddress(defendantEmail);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(MID, caseData)
                .pageId(PAGE_ID).build();

            List<String> errors = List.of("Invalid email format");

            when(validateEmailService.validate(defendantEmail)).thenReturn(errors);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEqualTo(errors);
        }
    }

    @Nested
    class AboutToSubmitTests {

        public static final String BEARER_TOKEN = "BEARER TOKEN";
        public static final String USER = "user";
        public static final String PASSWORD = "password";
        public static final String SEARCH_QUERY = String.format("email:\"%s\"", DEFENDANT_EMAIL);
        public static final String CITIZEN_CASE_ROLE = "citizen";

        private static @NonNull DefendantPinToPostLRspec getDefendantPin() {
            DefendantPinToPostLRspec defendantPin = new DefendantPinToPostLRspec();
            defendantPin.setExpiryDate(LocalDate.of(2026, 1, 1));
            defendantPin.setCitizenCaseRole(CITIZEN_CASE_ROLE);
            defendantPin.setRespondentCaseRole(CITIZEN_CASE_ROLE);
            return defendantPin;
        }

        @BeforeEach
        void setUp() {
            when(systemUpdateUserConfiguration.getUserName()).thenReturn(USER);
            when(systemUpdateUserConfiguration.getPassword()).thenReturn(PASSWORD);
            when(userService.getAccessToken(USER, PASSWORD)).thenReturn(BEARER_TOKEN);
            when(featureToggleService.isLinkDefendantTestingEnabled()).thenReturn(true);
        }

        @Test
        void shouldLinkDefendantToClaim_AndReturnNoError() {
            DefendantPinToPostLRspec defendantPin = getDefendantPin();
            defendantPin.setAccessCode("123");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted()
                .addRespondent1PinToPostLRspec(defendantPin).build();
            caseData.setDefendantEmailAddress(DEFENDANT_EMAIL);

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData).build();
            String userId = "1234567890";
            UserDetails userDetails = UserDetails.builder().id(userId).build();

            when(idamClient.searchUsers(BEARER_TOKEN, SEARCH_QUERY)).thenReturn(List.of(userDetails));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNullOrEmpty();
            Map<String, Object> responseData = response.getData();

            // Assert defendantUserDetails
            assertThat(responseData.get("defendantUserDetails"))
                .isNotNull()
                .satisfies(obj -> {
                    Map<String, Object> details = objectMapper.convertValue(obj, new TypeReference<>() {});
                    assertThat(details.get("id")).isEqualTo(userId);
                    assertThat(details.get("email")).isEqualTo(DEFENDANT_EMAIL);
                });

            // Assert respondent1 partyEmail
            assertThat(responseData.get("respondent1"))
                .isNotNull()
                .satisfies(obj -> {
                    Map<String, Object> respondent1 = objectMapper.convertValue(obj, new TypeReference<>() {});
                    assertThat(respondent1.get("partyEmail")).isEqualTo(DEFENDANT_EMAIL);
                });

            // Assert pin-in-post data cleaned
            assertThat(responseData.get("respondent1PinToPostLRspec"))
                .isNotNull()
                .satisfies(obj -> {
                    Map<String, Object> pin = objectMapper.convertValue(obj, new TypeReference<>() {});
                    assertThat(pin.get("expiryDate")).isEqualTo("2026-01-01");
                    assertThat(pin.get("citizenCaseRole")).isEqualTo(CITIZEN_CASE_ROLE);
                    assertThat(pin.get("respondentCaseRole")).isEqualTo(CITIZEN_CASE_ROLE);
                    assertThat(pin).doesNotContainKey("accessCode");
                });

            assertThat(responseData).doesNotContainKey("defendantEmailAddress");

            verify(coreCaseUserService).assignCase(
                CASE_ID.toString(),
                userId,
                null,
                CaseRole.DEFENDANT
            );
        }

        @Test
        void shouldNotLinkDefendantToClaim_AndReturnError() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            caseData.setDefendantEmailAddress(DEFENDANT_EMAIL);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData).build();

            when(idamClient.searchUsers(BEARER_TOKEN, SEARCH_QUERY)).thenReturn(List.of());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("No user found with the provided email address");
        }
    }
}
