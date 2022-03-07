package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    ChangeSolicitorEmailCallbackHandler.class,
    ValidateEmailService.class
})
class ChangeSolicitorEmailCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ChangeSolicitorEmailCallbackHandler handler;

    @Autowired
    private UserService userService;

    @MockBean
    private ValidateEmailService validateEmailService;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @BeforeEach
    void setupTest() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
    }

    @Nested
    class AboutToStart {
        CallbackParams params;

        @BeforeEach
        void setupAboutToStartTests() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .build();
            this.params = callbackParamsOf(caseData, ABOUT_TO_START);
        }

        @Test
        void shouldAmendSolicitorEmail_WhenInvokedByUserWith_Applicant1Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[APPLICANTSOLICITORONE]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("Yes", response.getData().get("isApplicant1"), "isApplicant1");
            assertEquals("No", response.getData().get("isRespondent1"), "isRespondent1");
            assertEquals("No", response.getData().get("isRespondent2"), "isRespondent2");
        }

        @Test
        void shouldSetPartyFlags_WhenInvokedByUserWith_Respondent1Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("No", response.getData().get("isApplicant1"), "isApplicant1");
            assertEquals("Yes", response.getData().get("isRespondent1"), "isRespondent1");
            assertEquals("No", response.getData().get("isRespondent2"), "isRespondent2");
        }

        @Test
        void shouldSetPartyFlags_WhenInvokedByUserWith_Respondent2Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORTWO]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(caseRoles);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("No", response.getData().get("isApplicant1"), "isApplicant1");
            assertEquals("No", response.getData().get("isRespondent1"), "isRespondent1");
            assertEquals("Yes", response.getData().get("isRespondent2"), "isRespondent2");
        }

        @Test
        void shouldSetPartyFlags_WhenInvokedByUserWith_Respondent1And2Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");
            caseRoles.add("[RESPONDENTSOLICITORTWO]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(caseRoles);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("No", response.getData().get("isApplicant1"), "isApplicant1");
            assertEquals("Yes", response.getData().get("isRespondent1"), "isRespondent1");
            assertEquals("Yes", response.getData().get("isRespondent2"), "isRespondent2");
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldReturnResponse_WithApplicant1SolicitorEmail() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build()
                ).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            var actual = ((HashMap)response.getData().get("applicantSolicitor1UserDetails")).get("email");
            assertEquals("applicant1solicitor@gmail.com", actual, "applicant1SolicitorEmail");
        }

        @Test
        void shouldReturnResponse_WithRespondent1SolicitorEmail() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor1EmailAddress("respondent1solicitor@gmail.com")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("respondent1solicitor@gmail.com",
                         response.getData().get("respondentSolicitor1EmailAddress"),
                "respondentSolicitor1EmailAddress");
        }

        @Test
        void shouldReturnResponse_WithRespondent2SolicitorEmail() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor2EmailAddress("respondent2solicitor@gmail.com")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("respondent2solicitor@gmail.com",
                         response.getData().get("respondentSolicitor2EmailAddress"),
                         "respondentSolicitor2EmailAddress");
        }

        @Test
        void shouldClearPartyFlags() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals(null, response.getData().get("isApplicant1"), "isApplicant1");
            assertEquals(null, response.getData().get("isRespondent1"), "isRespondent1");
            assertEquals(null, response.getData().get("isRespondent2"), "isRespondent2");
        }
    }

    @Nested
    class ValidateSolicitorEmail {

        ArrayList<String> mockErrors;

        @BeforeEach
        void setupTests() {
            mockErrors = new ArrayList<>();
            mockErrors.add("mock-error");
        }

        @Test
        void validateApplicant1SolicitorEmail_shouldInvokeEmailValidationServiceAndReturnOutput() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicantSolicitor1UserDetails(
                    IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-applicant1-solicitor-email");

            when(validateEmailService.validate(anyString())).thenReturn(mockErrors);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(validateEmailService, times(1))
                .validate("applicant1solicitor@gmail.com");
            assertEquals(mockErrors, response.getErrors(), "errors");
        }

        @Test
        void validateRespondent1SolicitorEmail_shouldInvokeEmailValidationServiceAndReturnOutput() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1EmailAddress("respondent1solicitor@gmail.com")
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-respondent1-solicitor-email");

            when(validateEmailService.validate(anyString())).thenReturn(mockErrors);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(validateEmailService, times(1))
                .validate("respondent1solicitor@gmail.com");
            assertEquals(mockErrors, response.getErrors(), "errors");
        }

        @Test
        void validateRespondent2SolicitorEmail_shouldInvokeEmailValidationServiceAndReturnOutput() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor2EmailAddress("respondent2solicitor@gmail.com")
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-respondent2-solicitor-email");

            when(validateEmailService.validate(anyString())).thenReturn(mockErrors);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(validateEmailService, times(1))
                .validate("respondent2solicitor@gmail.com");
            assertEquals(mockErrors, response.getErrors(), "errors");
        }
    }

    @Nested
    class Submitted {

        CaseData caseData;
        CallbackParams params;

        @BeforeEach
        void setupTests() {
            caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor1EmailAddress("respondent1solicitor@gmail.com")
                .build();
            params = callbackParamsOf(caseData, SUBMITTED);
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_WhenInvokedByUserWith_Applicant1Role() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[APPLICANTSOLICITORONE]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertEquals("# You have updated a claimant's legal representative's email address",
                         response.getConfirmationHeader(), "confirmationHeader");
            assertEquals("<br />",
                         response.getConfirmationBody(), "confirmationBody");
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_WhenInvokedByUserWith_Applicant2Role() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[APPLICANTSOLICITORTWO]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertEquals("# You have updated a claimant's legal representative's email address",
                         response.getConfirmationHeader(), "confirmationHeader");
            assertEquals("<br />",
                         response.getConfirmationBody(), "confirmationBody");
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_WhenInvokedByUserWith_Respondent1Role() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertEquals("# You have updated a defendant's legal representative's email address",
                         response.getConfirmationHeader(), "confirmationHeader");
            assertEquals("<br />",
                         response.getConfirmationBody(), "confirmationBody");
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_WhenInvokedByUserWith_Respondent2Role() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORTWO]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertEquals("# You have updated a defendant's legal representative's email address",
                         response.getConfirmationHeader(), "confirmationHeader");
            assertEquals("<br />",
                         response.getConfirmationBody(), "confirmationBody");
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_WhenInvokedByUserWith_Respondent1And2Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");
            caseRoles.add("[RESPONDENTSOLICITORTWO]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertEquals("# You have updated a defendant's legal representative's email address",
                         response.getConfirmationHeader(), "confirmationHeader");
            assertEquals("<br />",
                         response.getConfirmationBody(), "confirmationBody");
        }
    }
}
